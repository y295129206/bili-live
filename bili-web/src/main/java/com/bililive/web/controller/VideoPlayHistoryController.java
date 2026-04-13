package com.bililive.web.controller;

import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.service.VideoPlayHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/history")
public class VideoPlayHistoryController {
    @Autowired
    private VideoPlayHistoryService videoPlayHistoryService;
    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/loadHistory")
    public Result<PaginationResult<VideoPlayHistory>> loadHistory(Integer pageNo,HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoPlayHistoryQuery query = new VideoPlayHistoryQuery();
        query.setUserId(tokenUserInfoDto.getUserId());
        query.setPageNo(pageNo);
        query.setOrderBy("last_update_time desc");
        query.setQueryVideoDetail(true);
        PaginationResult<VideoPlayHistory> videoPlayHistoryList = videoPlayHistoryService.findListByPage(query);
        return Result.success(videoPlayHistoryList);
    }
    @RequestMapping("/cleanHistory")
    public Result cleanHistory(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoPlayHistoryQuery query = new VideoPlayHistoryQuery();
        query.setUserId(tokenUserInfoDto.getUserId());
        videoPlayHistoryService.deleteByParam(query);
        return Result.success();
    }

    @RequestMapping("/delHistory")
    public Result delHistory(@NotEmpty String videoId, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoPlayHistoryQuery query = new VideoPlayHistoryQuery();
        query.setUserId(tokenUserInfoDto.getUserId());
        query.setVideoId(videoId);
        videoPlayHistoryService.deleteByParam(query);
        return Result.success();
    }

    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
