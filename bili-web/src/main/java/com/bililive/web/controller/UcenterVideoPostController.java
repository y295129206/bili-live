package com.bililive.web.controller;

import com.bililive.Enum.VideoStatusEnum;
import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.service.VideoInfoFilePostService;
import com.bililive.service.VideoInfoPostService;
import com.bililive.service.VideoInfoService;
import com.bililive.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户中心控制器（视频发布相关）
 */
@RestController
@RequestMapping("/ucenter")
@Validated
@Slf4j
public class UcenterVideoPostController {

    @Autowired
    private VideoInfoPostService videoInfoPostService;
    @Autowired
    private VideoInfoFilePostService videoInfoFilePostService;
    @Autowired
    private VideoInfoService videoInfoService;
    @Autowired
    private RedisComponent redisComponent;
    @RequestMapping("/postVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result postVideo(String videoId,
                                    @NotEmpty String videoCover,
                                    @NotEmpty @Size(max=100) String videoName,
                                    @NotNull Integer pCategoryId,
                                    Integer categoryId,
                                    @NotNull Integer postType,
                                    @NotEmpty @Size(max=300) String tags,
                                    @Size(max = 2000) String introduction,
                                    @Size(max = 3) String interaction,
                                    @NotEmpty String uploadFileList,
                                    HttpServletRequest  request) {
        TokenUserInfoDto userInfo = getTokenUserInfoDto(request);
        List<VideoInfoFilePost> filePostList = JsonUtils.convertJsonArray2List(uploadFileList, VideoInfoFilePost.class);
        VideoInfoPost videoInfo = VideoInfoPost.builder()
                .videoId(videoId)
                .videoCover(videoCover)
                .videoName(videoName)
                .pCategoryId(pCategoryId)
                .categoryId(categoryId)
                .postType(postType)
                .tags(tags)
                .introduction(introduction)
                .interaction(interaction)
                .userId(userInfo.getUserId())
                .build();
        log.info("开始发布视频");
        videoInfoPostService.saveVideoInfo(videoInfo,filePostList);
        log.info("结束发布视频");
        return Result.success();
    }
    @RequestMapping("/loadVideoList")
    @GlobalInterceptor(checkLogin = true)
    public Result<PaginationResult> postVideo(Integer status, Integer pageNo, Integer pageSize, String videoNameFuzzy, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoPostQuery.setPageNo(pageNo != null ? pageNo : 1);
        videoInfoPostQuery.setPageSize(pageSize != null ? pageSize : 10);
        videoInfoPostQuery.setOrderBy("v.create_time desc");
        if(status!=null){
            if(status==-1){
                videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(),VideoStatusEnum.STATUS4.getStatus()});
            }else{
                videoInfoPostQuery.setStatus(status);
            }
        }
        videoInfoPostQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoInfoPostQuery.setQueryCountInfo(true);
        PaginationResult result = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return Result.success(result);
    }
    @RequestMapping("/getVideoCountInfo")
    @GlobalInterceptor(checkLogin = true)
    public Result<VideoCountInfo> getVideoCountInfo(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS3.getStatus());
        Integer auditPassCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS4.getStatus());
        Integer auditFailCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);

        videoInfoPostQuery.setStatus(null);
        videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(),VideoStatusEnum.STATUS4.getStatus()});
        Integer inProgress = videoInfoPostService.findCountByParam(videoInfoPostQuery);
        VideoCountInfo videoCountInfo = VideoCountInfo.builder()
                .auditPassCount(auditPassCount)
                .auditFailCount(auditFailCount)
                .inProgress(inProgress)
                .build();
        return Result.success(videoCountInfo);
    }

    @RequestMapping("getVideoByVideoId")
    @GlobalInterceptor(checkLogin = true)
    public Result<VideoPostEditInfo> getVideoByVideoId(@NotEmpty String videoId, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoInfoPost videoInfoPost = videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        if(videoInfoPost==null || !videoInfoPost.getUserId().equals(tokenUserInfoDto.getUserId())){
            throw new BaseException(404,"视频不存在");
        }
        VideoInfoFilePostQuery query = new VideoInfoFilePostQuery();
        query.setVideoId(videoId);
        query.setOrderBy("file_index asc");
        List<VideoInfoFilePost> videoInfoFilePostList = videoInfoFilePostService.findListByParam(query);
        VideoPostEditInfo videoInfo = new VideoPostEditInfo();
        videoInfo.setVideoInfo(videoInfoPost);
        videoInfo.setVideoInfoFileList(videoInfoFilePostList);
        return Result.success(videoInfo);
    }

    @RequestMapping("/saveVideoInteraction")
    @GlobalInterceptor(checkLogin = true)
    public Result saveVideoInteraction(@NotEmpty String videoId,
                                       String interaction,
                                       HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        videoInfoService.changeInteraction(videoId,tokenUserInfoDto.getUserId(),interaction);
        return Result.success();
    }

    @RequestMapping("/deleteVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result deleteVideo(@NotEmpty String videoId, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        videoInfoService.deleteVideo(videoId,tokenUserInfoDto.getUserId());
        return Result.success();
    }

    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
