package com.bililive.admin.controller;

import com.bililive.mapper.VideoCommentMapper;
import com.bililive.pojo.*;
import com.bililive.service.UserInfoService;
import com.bililive.service.VideoCommentService;
import com.bililive.service.VideoDanmuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/interact")
@Validated
@Slf4j
public class InteractController {
    @Autowired
    private VideoCommentService videoCommentService;
    @Autowired
    private VideoDanmuService videoDanmuService;

    @RequestMapping("/loadComment")
    public Result<PaginationResult<VideoComment>> loadComment(Integer pageNo, String videoNameFuzzy) {
        VideoCommentQuery query = new VideoCommentQuery();
        query.setPageNo(pageNo);
        query.setVideoNameFuzzy(videoNameFuzzy);
        query.setOrderBy("comment_id desc");
        query.setQueryVideoInfo( true);
        PaginationResult<VideoComment> videoInfoList = videoCommentService.findListByPage(query);
        return Result.success(videoInfoList);
    }

    @RequestMapping("/delComment")
    public Result<String> delComment(@NotNull Integer commentId) {
        videoCommentService.deleteComment(commentId,null);
        return Result.success("删除成功");
    }

    @RequestMapping("/loadDanmu")
    public Result<PaginationResult<VideoDanmu>> loadDanmu(Integer pageNo, String videoNameFuzzy) {
        VideoDanmuQuery query = new VideoDanmuQuery();
        query.setPageNo(pageNo);
        query.setVideoNameFuzzy(videoNameFuzzy);
        query.setOrderBy("danmu_id desc");
        query.setQueryVideoInfo(true);
        PaginationResult<VideoDanmu> videoInfoList = videoDanmuService.findListByPage(query);
        return Result.success(videoInfoList);
    }

    @RequestMapping("/delDanmu")
    public Result<String> delDanmu(@NotNull Integer danmuId) {
        videoDanmuService.deleteDanmu(danmuId,null);
        return Result.success("删除成功");
    }
}
