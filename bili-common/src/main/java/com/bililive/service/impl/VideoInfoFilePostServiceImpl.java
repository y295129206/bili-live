package com.bililive.service.impl;


import com.bililive.mapper.VideoInfoFilePostMapper;
import com.bililive.pojo.VideoInfoFilePost;
import com.bililive.pojo.VideoInfoFilePostQuery;
import com.bililive.service.VideoInfoFilePostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;

/**
 * 视频信息服务实现类
 */
@Service
@Slf4j
public class VideoInfoFilePostServiceImpl implements VideoInfoFilePostService {

    @Autowired
    private VideoInfoFilePostMapper videoInfoFilePostMapper;

    @Override
    public List<VideoInfoFilePost> findListByParam(VideoInfoFilePostQuery query) {
        return videoInfoFilePostMapper.selectList(query);
    }
}
