package com.bililive.web.controller;

import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.service.VideoDanmuService;
import com.bililive.service.VideoInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;

@RestController
@RequestMapping("/danmu")
public class VideoDanmuController {
    @Autowired
    private VideoDanmuService videoDanmuService;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private VideoInfoService videoInfoService;

    @RequestMapping("/postDanmu")
    @GlobalInterceptor(checkLogin = true)
    public Result postDanmu(@NotEmpty String videoId, @NotEmpty String fileId,
                            @NotEmpty @Size(max=200) String text, @NotEmpty Integer mode,
                            @NotEmpty String color, @NotNull Integer time,HttpServletRequest  request) {
        VideoDanmu danmu = new VideoDanmu();
        danmu.setVideoId(videoId);
        danmu.setFileId(fileId);
        danmu.setText(text);
        danmu.setMode(mode);
        danmu.setColor(color);
        danmu.setTime(time);
        danmu.setUserId(getTokenUserInfoDto( request).getUserId());
        danmu.setPostTime(new Date());

        videoDanmuService.saveVideoDanmu(danmu);
        return Result.success();

    }
    @RequestMapping("/loadDanmu")
    public Result loadDanmu(@NotEmpty String fileId, @NotEmpty String videoId) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if(videoInfo == null){
            return Result.success(new ArrayList<>());
        }
        if(videoInfo.getInteraction()!=null&&videoInfo.getInteraction().equals("1")){
            return Result.success(new ArrayList<>());
        }
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setFileId(fileId);
        videoDanmuQuery.setOrderBy("danmu_id asc");
        return Result.success(videoDanmuService.findListByParam(videoDanmuQuery));
    }
    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
