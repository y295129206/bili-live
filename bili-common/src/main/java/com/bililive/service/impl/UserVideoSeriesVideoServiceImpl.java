package com.bililive.service.impl;

import com.bililive.mapper.UserVideoSeriesMapper;
import com.bililive.mapper.UserVideoSeriesVideoMapper;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.UserVideoSeries;
import com.bililive.pojo.UserVideoSeriesVideo;
import com.bililive.pojo.UserVideoSeriesVideoQuery;
import com.bililive.service.UserVideoSeriesVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户视频合集-视频关联服务实现类
 */
@Service("userVideoSeriesVideoService")
@Slf4j
public class UserVideoSeriesVideoServiceImpl implements UserVideoSeriesVideoService {

    @Autowired
    private UserVideoSeriesVideoMapper userVideoSeriesVideoMapper;

    @Autowired
    private UserVideoSeriesMapper userVideoSeriesMapper;


    @Override
    public List<UserVideoSeriesVideo> findListByParam(UserVideoSeriesVideoQuery query) {
        if (query == null) {
            query = new UserVideoSeriesVideoQuery();
        }
        return userVideoSeriesVideoMapper.selectList(query);
    }
}
