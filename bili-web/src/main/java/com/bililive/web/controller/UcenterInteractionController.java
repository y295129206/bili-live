package com.bililive.web.controller;

import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.mapper.VideoInfoFilePostMapper;
import com.bililive.pojo.*;
import com.bililive.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/ucenter")
@Validated
@Slf4j
public class UcenterInteractionController {
    @Autowired
    private VideoInfoService videoInfoService;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private VideoCommentService videoCommentService;
    @Autowired
    private VideoDanmuService videoDanmuService;
    @Autowired
    private StatisticsInfoService statisticsInfoService;

    @RequestMapping("/loadAllVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result<List<VideoInfo>> loadAllVideo(HttpServletRequest  request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoQuery.setOrderBy("create_time desc");
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(videoInfoQuery);
        return Result.success(videoInfoList);
    }

    @RequestMapping("/loadComment")
    @GlobalInterceptor(checkLogin = true)
    public Result<PaginationResult<VideoComment>> loadComment(Integer pageNo, String videoId,HttpServletRequest  request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoUserId(tokenUserInfoDto.getUserId());
        videoCommentQuery.setOrderBy("comment_id desc");
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setQueryVideoInfo(true);
        PaginationResult<VideoComment> result = videoCommentService.findListByPage(videoCommentQuery);
        return Result.success(result);
    }

    @RequestMapping("/delComment")
    @GlobalInterceptor(checkLogin = true)
    public Result delComment(@NotNull Integer commentId, HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        videoCommentService.deleteComment(commentId,tokenUserInfoDto.getUserId());
        return Result.success();
    }

    @RequestMapping("/loadDanmu")
    @GlobalInterceptor(checkLogin = true)
    public Result<PaginationResult<VideoDanmu>> loadDanmu(Integer pageNo,String videoId,HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setVideoUserId(tokenUserInfoDto.getUserId());
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setOrderBy("danmu_id desc");
        videoDanmuQuery.setQueryVideoInfo(true);
        PaginationResult<VideoDanmu> result = videoDanmuService.findListByPage(videoDanmuQuery);
        return Result.success(result);
    }

    @RequestMapping("/delDanmu")
    @GlobalInterceptor(checkLogin = true)
    public Result delDanmu(@NotNull Integer danmuId, HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        videoDanmuService.deleteDanmu(danmuId,tokenUserInfoDto.getUserId());
        return Result.success();
    }
    @RequestMapping("/test")
    public Result test(){
        statisticsInfoService.statisticsData();
        return Result.success();
    }
    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
