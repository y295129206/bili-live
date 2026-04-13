package com.bililive.service.impl;

import com.bililive.mapper.VideoInfoFileMapper;
import com.bililive.pojo.SimplePage;
import com.bililive.pojo.VideoInfoFile;
import com.bililive.pojo.VideoInfoFileQuery;
import com.bililive.service.VideoInfoFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 视频文件信息服务实现类
 */
@Service
@Slf4j
public class VideoInfoFileServiceImpl implements VideoInfoFileService {

    @Autowired
    private VideoInfoFileMapper videoInfoFileMapper;

    @Override
    public List<VideoInfoFile> findListByParam(VideoInfoFileQuery query) {
        if (query == null) {
            query = new VideoInfoFileQuery();
        }
        
        // 设置默认分页参数
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }
        
        // 创建 SimplePage 对象用于 MyBatis 分页
        SimplePage simplePage = new SimplePage(query.getPageNum(), query.getPageSize());
        query.setSimplePage(simplePage);
        
        return videoInfoFileMapper.selectList(query);
    }
}
