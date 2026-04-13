package com.bililive.service;

import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.VideoPlayHistory;
import com.bililive.pojo.VideoPlayHistoryQuery;

import java.util.List;

public interface VideoPlayHistoryService {

    /**
     * 保存播放历史
     * @param userId 用户ID
     * @param videoId 视频ID
     * @param fileIndex 文件索引
     */
    void saveHistory(String userId, String videoId, Integer fileIndex);

    /**
     * 分页查询播放历史列表
     * @param query 查询条件
     * @return 分页结果
     */
    PaginationResult<VideoPlayHistory> findListByPage(VideoPlayHistoryQuery query);

    /**
     * 根据查询条件删除播放历史
     * @param query 查询条件
     */
    void deleteByParam(VideoPlayHistoryQuery query);
}
