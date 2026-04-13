package com.bililive.service;

import com.bililive.pojo.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 视频信息服务接口
 */
public interface VideoInfoPostService {

    void saveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> uploadFileList);

    void transferVideoFile(VideoInfoFilePost videoInfoFile);

    /**
     * 分页查询视频列表
     */
    PaginationResult<VideoInfoPost> findListByPage(VideoInfoPostQuery query);

    /**
     * 根据条件查询视频数量
     */
    Integer findCountByParam(VideoInfoPostQuery query);

    void auditVideo(@NotEmpty String videoId, @NotNull Integer status, String reason);

    VideoInfoPost getVideoInfoPostByVideoId(@NotEmpty String videoId);
}

