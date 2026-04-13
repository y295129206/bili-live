package com.bililive.component;

import com.bililive.Enum.DateTimePatternEnum;
import com.bililive.Enum.SearchOrderTypeEnum;
import com.bililive.entity.config.AppConfig;
import com.bililive.mapper.UserInfoMapper;
import com.bililive.pojo.*;
import com.bililive.utils.CopyTools;
import com.bililive.utils.DateUtils;
import com.bililive.utils.JsonUtils;
import com.bililive.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("esSearchComponent")
@Slf4j
public class EsSearchComponent {
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private UserInfoMapper userInfoMapper;

    private Boolean isExistIndex() throws Exception{
        GetIndexRequest getIndexRequest = new GetIndexRequest(appConfig.getEsIndexVideoName());
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }
    public void createIndex(){
        try{
            if(isExistIndex()){
                return;
            }
            CreateIndexRequest request = new CreateIndexRequest(appConfig.getEsIndexVideoName());
            request.settings("{\"analysis\": {\"analyzer\": {\"comma\": {\"type\": \"pattern\",\"pattern\": \",\"}}}}", XContentType.JSON);
            request.mapping("{" +
                    "\"properties\": {" +
                    "\"videoId\": {\"type\": \"text\", \"index\": false}," +
                    "\"userId\": {\"type\": \"text\", \"index\": false}," +
                    "\"videoCover\": {\"type\": \"text\", \"index\": false}," +
                    "\"videoName\": {\"type\": \"text\", \"analyzer\": \"ik_max_word\"}," +
                    "\"tags\": {\"type\": \"text\", \"analyzer\": \"comma\"}," +
                    "\"playCount\": {\"type\": \"integer\", \"index\": false}," +
                    "\"danmuCount\": {\"type\": \"integer\", \"index\": false}," +
                    "\"collectCount\": {\"type\": \"integer\", \"index\": false}," +
                    "\"createTime\": {\"type\": \"date\", \"format\": \"yyyy-MM-dd HH:mm:ss\", \"index\": false}" +
                    "}" +
                    "}", XContentType.JSON);
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            Boolean acknowledged = createIndexResponse.isAcknowledged();
            if(!acknowledged){
                throw new BaseException("初始化es失败");
            }
        }catch (Exception e){
            log.error("初始化es失败",e);
            throw new BaseException("初始化es失败");
        }
    }
    public void saveDoc(VideoInfo videoInfo){
        try {
            if(!docExist(videoInfo.getVideoId())){
                VideoInfoEsDto videoInfoEsDto = CopyTools.copy(videoInfo,VideoInfoEsDto.class);
                videoInfoEsDto.setCollectCount(0);
                videoInfoEsDto.setDanmuCount(0);
                videoInfoEsDto.setPlayCount(0);
                IndexRequest request = new IndexRequest(appConfig.getEsIndexVideoName());
                request.id(videoInfo.getVideoId()).source(JsonUtils.convertObj2Json(videoInfoEsDto), XContentType.JSON);
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
            }else{
                updateDoc(videoInfo);
            }
        } catch (Exception e) {
            log.error("保存es失败",e);
            throw new BaseException("保存es失败");
        }

    }
    public Boolean docExist(String videoId) throws IOException {
        GetRequest request = new GetRequest(appConfig.getEsIndexVideoName(), videoId);
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        return response.isExists();
    }
    private void updateDoc(VideoInfo videoInfo) throws Exception {
        try {
            videoInfo.setLastUpdateTime(null);
            videoInfo.setCreateTime(null);
            Map<String,Object> dataMap = new HashMap<>();
            Field[] fields = videoInfo.getClass().getDeclaredFields();
            for (Field field : fields) {
                // 跳过静态字段、final字段和合成字段
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    java.lang.reflect.Modifier.isFinal(field.getModifiers()) ||
                    field.isSynthetic()) {
                    continue;
                }
                field.setAccessible(true); // 设置可访问私有字段
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = videoInfo.getClass().getMethod(methodName);
                Object value = method.invoke(videoInfo);
                if((value != null && value instanceof String && !"".equals(value)) || 
                   (value != null && !(value instanceof String))){
                    dataMap.put(field.getName(),value);
                }
            }
            if(dataMap.isEmpty()){
                return;
            }
            UpdateRequest request = new UpdateRequest(appConfig.getEsIndexVideoName(), videoInfo.getVideoId());
            request.doc(dataMap);
            restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("更新es视频失败",e);
            throw new BaseException("更新es视频失败");
        }
    }

    public void updateDocCount(String videoId, String fieldName, Integer count) {
        try {
            UpdateRequest request = new UpdateRequest(appConfig.getEsIndexVideoName(), videoId);
            // 使用 painless 脚本进行原子性递增操作
            String scriptSource = "ctx._source." + fieldName + " += params.count";
            Map<String, Object> params = Collections.singletonMap("count", count);
            Script script = new Script(ScriptType.INLINE, "painless", scriptSource, params);
            request.script(script);
            restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("更新es视频计数失败",e);
            throw new BaseException("更新es视频计数失败");
        }
    }
    public void deleteDoc(String videoId) {
        try {
            DeleteRequest request = new DeleteRequest(appConfig.getEsIndexVideoName(), videoId);
            restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("删除es视频失败",e);
            throw new BaseException("删除es视频失败");
        }
    }
    public PaginationResult<VideoInfo> search(Boolean highlight, String keyword,Integer orderType,Integer pageNo,Integer pageSize){
        try {
            SearchOrderTypeEnum searchOrderTypeEnum = SearchOrderTypeEnum.getByType(orderType);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "videoName", "tags"));

            if(highlight){
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                highlightBuilder.field("videoName");
                highlightBuilder.preTags("<span class='highlight'>");
                highlightBuilder.postTags("</span>");
                sourceBuilder.highlighter(highlightBuilder);
            }
            sourceBuilder.sort("_score", SortOrder.DESC);
            if(orderType != null){
                sourceBuilder.sort(searchOrderTypeEnum.getField(), SortOrder.DESC);
            }
            pageNo = pageNo == null ? 1 : pageNo;
            pageSize = pageSize == null ? 20 : pageSize;
            sourceBuilder.size(pageSize);
            sourceBuilder.from((pageNo - 1) * pageSize);
            SearchRequest request = new SearchRequest(appConfig.getEsIndexVideoName());
            request.source(sourceBuilder);

            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            SearchHits hits = response.getHits();
            Integer totalCount = (int) hits.getTotalHits().value;
            List<VideoInfo> videoInfoList = new ArrayList<>();
            List<String> userIdList = new ArrayList<>();

            for (SearchHit hit : hits.getHits()) {
                VideoInfo videoInfo = JsonUtils.convertJson2Obj(hit.getSourceAsString(), VideoInfo.class);
                // 设置高亮显示的 videoName
                if(hit.getHighlightFields().get("videoName") != null){
                    videoInfo.setVideoName(hit.getHighlightFields().get("videoName").fragments()[0].toString());
                }
                videoInfoList.add(videoInfo);
                userIdList.add(videoInfo.getUserId());
            }
            UserInfoQuery userInfoQuery =new UserInfoQuery();
            userInfoQuery.setUserIdList(userIdList);
            List<UserInfo> userInfoList = userInfoMapper.selectList(userInfoQuery);
            Map<String,UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(item->item.getUserId(), Function.identity(),(data1,data2)->data2));

            videoInfoList.forEach(item->{
                UserInfo userInfo = userInfoMap.get(item.getUserId());
                item.setNickName(userInfo==null?"":userInfo.getNickName());
            });
            SimplePage simplePage = new SimplePage(pageNo,totalCount,pageSize);
            PaginationResult<VideoInfo> result = new PaginationResult<>(totalCount, simplePage.getPageSize(), simplePage.getPageNo(), simplePage.getTotalCount(), videoInfoList);
            return result;
        } catch (IOException e) {
            log.error("搜索es视频失败",e);
            throw new BaseException("搜索es视频失败");
        }
    }
}
