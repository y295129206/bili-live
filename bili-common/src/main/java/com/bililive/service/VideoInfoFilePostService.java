package com.bililive.service;

import com.bililive.pojo.VideoInfo;
import com.bililive.pojo.VideoInfoFilePost;
import com.bililive.pojo.VideoInfoFilePostQuery;

import java.util.List;
import java.util.Map;

/**
 * 视频信息服务接口
 */
public interface VideoInfoFilePostService {

    List<VideoInfoFilePost> findListByParam(VideoInfoFilePostQuery query);
}

