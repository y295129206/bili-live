package com.bililive.service;

import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.VideoInfo;
import com.bililive.pojo.VideoInfoQuery;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 视频信息服务接口
 */
public interface VideoInfoService {
    /**
     * 根据查询条件获取视频列表
     * @param query 查询参数
     * @return 视频列表
     */
    List<VideoInfo> findListByParam(VideoInfoQuery query);

    /**
     * 根据查询条件获取视频总数
     * @param query 查询参数
     * @return 视频总数
     */
    Integer findCountByParam(VideoInfoQuery query);


    /**
     * 分页查询视频列表
     * @param videoInfoQuery 查询参数
     * @return 分页结果
     */
    PaginationResult<VideoInfo> findListByPage(VideoInfoQuery videoInfoQuery);

    /**
     * 根据视频 ID 获取视频信息
     * @param videoId 视频 ID
     * @return 视频信息
     */
    VideoInfo getVideoInfoByVideoId(String videoId);

    void changeInteraction(String videoId, String userId, String interaction);

    void deleteVideo(@NotEmpty String videoId, String userId);

    void addReadCount(String videoId);

    void recommendVideo(@NotEmpty String videoId);
}

