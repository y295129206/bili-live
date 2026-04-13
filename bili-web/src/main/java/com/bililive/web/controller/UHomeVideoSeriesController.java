package com.bililive.web.controller;

import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/uhome/series")
@Slf4j
@Validated
public class UHomeVideoSeriesController {
    @Autowired
    private VideoInfoService videoInfoService;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserVideoSeriesService userVideoSeriesService;
    @Autowired
    private UserVideoSeriesVideoService userVideoSeriesVideoService;

    @RequestMapping("/loadVideoSeries")
    public Result<List<UserVideoSeries>> loadVideoSeries(@NotEmpty String userId, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        List<UserVideoSeries> userVideoSeriesList = userVideoSeriesService.getUserAllSeries(userId);
        return Result.success(userVideoSeriesList);
    }

    @RequestMapping("/saveVideoSeries")
    @GlobalInterceptor(checkLogin = true)
    public Result saveVideoSeries(Integer seriesId, @NotEmpty @Size(max = 100) String seriesName,
                                  @Size(max = 200) String seriesDescription,String videoIds,HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserVideoSeries userVideoSeries = new UserVideoSeries();
        userVideoSeries.setSeriesId(seriesId);
        userVideoSeries.setSeriesName(seriesName);
        userVideoSeries.setSeriesDescription(seriesDescription);
        userVideoSeries.setUserId(tokenUserInfoDto.getUserId());
        log.info("保存视频合集{}", videoIds);
        userVideoSeriesService.saveUserVideoSeries(userVideoSeries, videoIds);
        return Result.success();
    }
    @RequestMapping("/loadAllVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result<List<VideoInfo>> loadAllVideo(Integer seriesId, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if(seriesId!=null){
            UserVideoSeriesVideoQuery userVideoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
            userVideoSeriesVideoQuery.setSeriesId(seriesId);
            userVideoSeriesVideoQuery.setUserId(tokenUserInfoDto.getUserId());
            List<UserVideoSeriesVideo> userVideoSeriesVideoList = userVideoSeriesVideoService.findListByParam(userVideoSeriesVideoQuery);
            List<String> videoIdList = userVideoSeriesVideoList.stream().map(item->item.getVideoId()).toList();
            videoInfoQuery.setExcludeVideoIdArray(videoIdList.toArray(new String[videoIdList.size()]));
        }
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(videoInfoQuery);
        return Result.success(videoInfoList);

    }

    @RequestMapping("/getVideoSeriesDetail")
    public Result<UserVideoSeriesDetail> getVideoSeriesDetail(@NotNull Integer seriesId, HttpServletRequest request) {
        UserVideoSeries userVideoSeries = userVideoSeriesService.getUserVideoSeriesBySeriesId(seriesId);
        if(userVideoSeries==null){
            throw new BaseException(404,"未找到该视频合集");
        }
        UserVideoSeriesVideoQuery userVideoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
        userVideoSeriesVideoQuery.setSeriesId(seriesId);
        userVideoSeriesVideoQuery.setQueryVideoInfo( true);
        userVideoSeriesVideoQuery.setOrderBy("sort asc");
        List<UserVideoSeriesVideo> userVideoSeriesVideoList = userVideoSeriesVideoService.findListByParam(userVideoSeriesVideoQuery);
        return Result.success(new UserVideoSeriesDetail(userVideoSeries,userVideoSeriesVideoList));
    }

    @RequestMapping("/saveSeriesVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result saveVideoSeriesVideo(@NotNull Integer seriesId,@NotEmpty String videoIds,
                                       HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        userVideoSeriesService.saveSeriesVideo(tokenUserInfoDto.getUserId(),seriesId,videoIds);
        return Result.success();
    }

    @RequestMapping("/delSeriesVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result delSeriesVideo(@NotNull Integer seriesId,@NotEmpty String videoId,
                                 HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        userVideoSeriesService.delSeriesVideo(tokenUserInfoDto.getUserId(),seriesId,videoId);
        return Result.success();
    }

    @RequestMapping("/delVideoSeries")
    @GlobalInterceptor(checkLogin = true)
    public Result delVideoSeries(@NotNull Integer seriesId, HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        userVideoSeriesService.delVideoSeries(tokenUserInfoDto.getUserId(),seriesId);
        return Result.success();
    }

    @RequestMapping("/changeVideoSeriesSort")
    @GlobalInterceptor(checkLogin = true)
    public Result changeVideoSeriesSort(@NotNull String seriesIds,HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        userVideoSeriesService.changeVideoSeriesSort(tokenUserInfoDto.getUserId(),seriesIds);
        return Result.success();
    }

    @RequestMapping("/loadVideoSeriesWithVideo")
    public Result<List<UserVideoSeries>> loadVideoSeriesWithVideo(@NotNull String userId){
        UserVideoSeriesQuery query = new UserVideoSeriesQuery();
        query.setUserId(userId);
        query.setOrderBy("sort asc");
        List<UserVideoSeries> userVideoSeriesList = userVideoSeriesService.findListWithVideoList(query);
        return Result.success(userVideoSeriesList);
    }

    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
