package com.bililive.web.controller;

import com.bililive.Enum.SearchOrderTypeEnum;
import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.Enum.VideoRecommendTypeEnum;
import com.bililive.component.EsSearchComponent;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.service.UserActionService;
import com.bililive.service.VideoInfoFileService;
import com.bililive.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/video")
@Validated
public class VideoController {
    @Autowired
    private VideoInfoService videoInfoService;
    @Autowired
    private VideoInfoFileService videoInfoFileService;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserActionService userActionService;
    @Autowired
    private EsSearchComponent esSearchComponent;

    @RequestMapping("/loadRecommendVideo")
    public Result<List<VideoInfo>> loadRecommendVideo() {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.RECOMMEND.getType());
        List<VideoInfo> recommendVideoList = videoInfoService.findListByParam(videoInfoQuery);
        return Result.success(recommendVideoList);
    }

    @RequestMapping("/loadVideo")
    public Result<PaginationResult<VideoInfo>> loadRecommendVideo(Integer pCategoryId, Integer categoryId,Integer pageNo) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setPCategoryId(pCategoryId);
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.NOT_RECOMMEND.getType());
        PaginationResult<VideoInfo> paginationResult = videoInfoService.findListByPage(videoInfoQuery);
        return Result.success(paginationResult);
    }

    @RequestMapping("/getVideoInfo")
    public Result<VideoDetailResponse> getVideoInfo(@NotEmpty String videoId, HttpServletRequest request) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if(videoInfo==null){
            throw new BaseException(404,"视频不存在");
        }

        VideoDetailResponse response = new VideoDetailResponse();
        response.setVideoInfo(videoInfo);
        List<UserAction> userActionList = new ArrayList<>();
        if(getTokenUserInfoDto( request)!=null){
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setUserId(getTokenUserInfoDto( request).getUserId());
            userActionQuery.setVideoId(videoId);
            userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(),UserActionTypeEnum.VIDEO_COLLECT.getType(),
                    UserActionTypeEnum.VIDEO_COIN.getType()});
            userActionList = userActionService.findListByParam(userActionQuery);
        }
        response.setUserActionList(userActionList);
        return Result.success(response);
    }
    @RequestMapping("/loadVideoPList")
    public Result<List<VideoInfoFile>> loadVideoList(@NotEmpty String videoId) {
        VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
        videoInfoFileQuery.setVideoId(videoId);
        videoInfoFileQuery.setOrderBy("file_index asc");
        List<VideoInfoFile> videoInfoFileList = videoInfoFileService.findListByParam(videoInfoFileQuery);
        return Result.success(videoInfoFileList);
    }
    @RequestMapping("/reportVideoPlayOnline")
    public Result<Integer> reportVideoPlayOnline(@NotEmpty String fileId,@NotEmpty String deviceId) {
        return Result.success(redisComponent.reportVideoPlayOnline(fileId,deviceId));
    }

    @RequestMapping("/search")
    public Result<PaginationResult<VideoInfo>> search(@NotEmpty String keyword,Integer orderType,Integer pageNo) {
        redisComponent.addKeywordCount(keyword);
        // es搜索
        PaginationResult<VideoInfo> result = esSearchComponent.search(true, keyword,orderType,pageNo,30);
        return Result.success(result);
    }
    @RequestMapping("/getVideoRecommend")
    public Result<List<VideoInfo>> getVideoRecommend (@NotEmpty String keyword,@NotEmpty String videoId) {
        List<VideoInfo> recommendVideoList = esSearchComponent.search(false,keyword, SearchOrderTypeEnum.PLAY_COUNT.getType(),
                1, 10).getList();
        recommendVideoList = recommendVideoList.stream().filter(videoInfo -> !videoId.equals(videoInfo.getVideoId())).toList();
        return Result.success(recommendVideoList);
    }

    @RequestMapping("/getSearchKeywordTop")
    public Result<List<String>> getSearchKeywordTop() {
        List<String> topList = redisComponent.getKeywordTop(10);
        return Result.success(topList);
    }

    @RequestMapping("/loadHotVideoList")
    public Result<PaginationResult<VideoInfo>> loadHotVideoList(Integer pageNo) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setOrderBy("play_count desc");
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setLastPlayHour(24);
        PaginationResult<VideoInfo> result = videoInfoService.findListByPage(videoInfoQuery);
        return Result.success(result);
    }
    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
