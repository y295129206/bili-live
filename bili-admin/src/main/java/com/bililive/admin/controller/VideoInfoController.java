package com.bililive.admin.controller;

import com.bililive.Enum.MessageTypeEnum;
import com.bililive.Enum.VideoStatusEnum;
import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.Result;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.pojo.VideoInfoPostQuery;
import com.bililive.service.VideoInfoFilePostService;
import com.bililive.service.VideoInfoPostService;
import com.bililive.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/videoInfo")
@Slf4j
@Validated
public class VideoInfoController {
    @Autowired
    private VideoInfoPostService videoInfoPostService;
    @Autowired
    private VideoInfoService videoInfoService;

    @RequestMapping("/loadVideoList")
    public Result<PaginationResult> loadVideoList(VideoInfoPostQuery videoInfoPostQuery) {
        videoInfoPostQuery.setOrderBy("v.last_update_time desc");
        videoInfoPostQuery.setQueryCountInfo(true);
        videoInfoPostQuery.setQueryUserInfo(true);
        PaginationResult result = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return Result.success(result);
    }
    @RequestMapping("/auditVideo")
    @GlobalInterceptor(messageType = MessageTypeEnum.SYSTEM)
    public Result<String> auditVideo(@NotEmpty String videoId, @NotNull Integer status,String reason) {
        videoInfoPostService.auditVideo(videoId, status,reason);
        return Result.success("审核成功");
    }
    @RequestMapping("/recommendVideo")
    public Result<String> recommendVideo(@NotEmpty String videoId) {
        videoInfoService.recommendVideo(videoId);
        return Result.success("审核成功");
    }
    @RequestMapping("/deleteVideo")
    public Result<String> deleteVideo(@NotEmpty String videoId) {
        videoInfoService.deleteVideo(videoId,null);
        return Result.success("删除成功");
    }
}
