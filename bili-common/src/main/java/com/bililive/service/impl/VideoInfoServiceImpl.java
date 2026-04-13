package com.bililive.service.impl;

import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.Enum.VideoRecommendTypeEnum;
import com.bililive.component.EsSearchComponent;
import com.bililive.component.RedisComponent;
import com.bililive.entity.config.AppConfig;
import com.bililive.entity.constants.Constants;
import com.bililive.mapper.*;
import com.bililive.pojo.*;
import com.bililive.service.UserActionService;
import com.bililive.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 视频信息服务实现类
 */
@Service
@Slf4j
public class VideoInfoServiceImpl implements VideoInfoService {

    private static ExecutorService exceutorService = Executors.newFixedThreadPool(10);
    @Autowired
    private VideoInfoMapper videoInfoMapper;
    @Autowired
    private VideoInfoPostMapper videoInfoPostMapper;
    @Autowired
    private VideoInfoFileMapper videoInfoFileMapper;
    @Autowired
    private VideoInfoFilePostMapper videoInfoFilePostMapper;
    @Autowired
    private VideoDanmuMapper videoDanmuMapper;
    @Autowired
    private VideoCommentMapper videoCommentMapper;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private EsSearchComponent esSearchComponent;
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public List<VideoInfo> findListByParam(VideoInfoQuery query) {
        if (query == null) {
            query = new VideoInfoQuery();
        }
        
        // 设置默认分页参数
        if (query.getPageNo() == null || query.getPageNo() < 1) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }
        
        // 创建 SimplePage 对象用于 MyBatis 分页
        SimplePage simplePage = new SimplePage(query.getPageNo(), query.getPageSize());
        query.setSimplePage(simplePage);
        
        return videoInfoMapper.selectList(query);
    }

    @Override
    public Integer findCountByParam(VideoInfoQuery query) {
        if (query == null) {
            query = new VideoInfoQuery();
        }
        return videoInfoMapper.selectCount(query);
    }

    @Override
    public PaginationResult<VideoInfo> findListByPage(VideoInfoQuery query) {
        if (query == null) {
            query = new VideoInfoQuery();
        }

        // 设置默认分页参数
        if (query.getPageNo() == null || query.getPageNo() < 1) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }

        // 创建 SimplePage 对象用于 MyBatis 分页
        SimplePage simplePage = new SimplePage(query.getPageNo(), query.getPageSize());
        query.setSimplePage(simplePage);
        
        // 查询数据列表
        List<VideoInfo> list = videoInfoMapper.selectList(query);
        
        // 查询总数
        Integer totalCount = videoInfoMapper.selectCount(query);
        
        // 计算总页数
        int totalPages = (totalCount + query.getPageSize() - 1) / query.getPageSize();

        PaginationResult<VideoInfo> paginationResult = new PaginationResult<>();
        paginationResult.setList(list);
        paginationResult.setTotalCount(totalCount);
        paginationResult.setTotalPages(totalPages);
        paginationResult.setPageNo(query.getPageNo());
        paginationResult.setPageSize(query.getPageSize());
        // 构建分页结果
        return paginationResult;
    }

    @Override
    public VideoInfo getVideoInfoByVideoId(String videoId) {
        if (videoId == null || videoId.isEmpty()) {
            throw new BaseException("视频 ID 不能为空");
        }
        return videoInfoMapper.selectByVideoId(videoId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInteraction(String videoId, String userId, String interaction) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
        videoInfo.setInteraction(interaction);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoId(videoId);
        videoInfoQuery.setUserId(userId);
        videoInfoMapper.updateByParam(videoInfo, videoInfoQuery);

        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setInteraction(interaction);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setVideoId(videoId);
        videoInfoPostQuery.setUserId(userId);
        videoInfoPostMapper.updateByParam(videoInfoPost, videoInfoPostQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVideo(String videoId, String userId) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
        if(videoInfo == null || (userId != null && !userId.equals(videoInfo.getUserId()))){
            throw new BaseException(404,"视频不存在或无权限删除");
        }
        List<VideoInfoFile> videoInfoFileList = videoInfoPostMapper.selectListByVideoId(videoId);
        // 先删除主表数据
        videoInfoMapper.deleteByVideoId(videoId);
        videoInfoPostMapper.deleteByVideoId(videoId);
        
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        userInfoMapper.updateCoinCountByUserId(videoInfo.getUserId(), -sysSettingDto.getPostVideoCoinCount());

        esSearchComponent.deleteDoc(videoId);
        // 异步删除关联数据
        exceutorService.execute(() -> {

                // 删除视频文件记录
                videoInfoFileMapper.deleteByVideoId(videoId);
                videoInfoFilePostMapper.deleteByVideoId(videoId);
                
                // 删除弹幕
                videoDanmuMapper.deleteByVideoId(videoId);
                
                // 删除评论
                videoCommentMapper.deleteByVideoId(videoId);

                for(VideoInfoFile videoInfoFile : videoInfoFileList){
                    try{
                        File file = new File(appConfig.getProjectFolder() +Constants.FILE_FOLDER+ videoInfoFile.getFilePath());
                        log.info("删除文件,{}", appConfig.getProjectFolder() +Constants.FILE_FOLDER+ videoInfoFile.getFilePath());
                        FileUtils.deleteDirectory(file);
                    }catch(IOException e){
                        log.error("删除文件失败,{}", videoInfoFile.getFilePath());
                    }
                }

        });

    }
    @Override
    public void addReadCount(String videoId){
        log.info("增加视频播放次数,videoId:{}", videoId);
        videoInfoMapper.updateCountInfo(videoId, UserActionTypeEnum.VIDEO_PLAY.getField(), 1);
    }

    @Override
    public void recommendVideo(String videoId) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
        if(videoInfo == null){
            throw new BaseException(600, "视频不存在");
        }
        Integer recommendType = null;
        if(VideoRecommendTypeEnum.RECOMMEND.getType().equals(videoInfo.getRecommendType())){
            recommendType = VideoRecommendTypeEnum.NOT_RECOMMEND.getType();
        }else{
            recommendType = VideoRecommendTypeEnum.RECOMMEND.getType();
        }
        VideoInfo updateVideoInfo = new VideoInfo();
        updateVideoInfo.setRecommendType(recommendType);
        videoInfoMapper.updateByVideoId(updateVideoInfo, videoId);
    }

}
