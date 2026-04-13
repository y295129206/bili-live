package com.bililive.service;

import com.bililive.pojo.VideoInfoFile;
import com.bililive.pojo.VideoInfoFileQuery;

import java.util.List;

/**
 * 视频文件信息服务接口
 */
public interface VideoInfoFileService {
    /**
     * 根据查询条件获取视频文件列表
     * @param query 查询参数
     * @return 视频文件列表
     */
    List<VideoInfoFile> findListByParam(VideoInfoFileQuery query);
}
