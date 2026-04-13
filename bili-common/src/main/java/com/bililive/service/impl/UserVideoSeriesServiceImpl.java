package com.bililive.service.impl;

import com.bililive.mapper.UserVideoSeriesMapper;
import com.bililive.mapper.UserVideoSeriesVideoMapper;
import com.bililive.mapper.VideoInfoMapper;
import com.bililive.pojo.*;
import com.bililive.service.UserVideoSeriesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户视频合集服务实现类
 */
@Service("userVideoSeriesService")
@Slf4j
public class UserVideoSeriesServiceImpl implements UserVideoSeriesService {

    @Autowired
    private UserVideoSeriesMapper userVideoSeriesMapper;

    @Autowired
    private UserVideoSeriesVideoMapper userVideoSeriesVideoMapper;

    @Autowired
    private VideoInfoMapper videoInfoMapper;

    @Override
    public List<UserVideoSeries> getUserAllSeries(String userId) {
        return userVideoSeriesMapper.selectUserAllSeries(userId);
    }

    @Override
    public UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId) {
        if (seriesId == null) {
            throw new BaseException(600, "合集 ID 不能为空");
        }
        try {

            return userVideoSeriesMapper.selectBySeriesId(seriesId);
        } catch (NumberFormatException e) {
            throw new BaseException(600, "合集 ID 格式错误");
        }
    }

    @Override
    public void delSeriesVideo(String userId, Integer seriesId, String videoId) {
        UserVideoSeriesVideoQuery query = new UserVideoSeriesVideoQuery();
        query.setSeriesId(seriesId);
        query.setVideoId(videoId);
        query.setUserId(userId);
        userVideoSeriesVideoMapper.deleteByParam(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delVideoSeries(String userId, Integer seriesId) {
        UserVideoSeriesQuery query = new UserVideoSeriesQuery();
        query.setSeriesId(seriesId);
        query.setUserId(userId);
        Integer count = userVideoSeriesMapper.deleteByParam(query);
        if (count == 0) {
            throw new BaseException(600, "合集不存在");
        }
        UserVideoSeriesVideoQuery videoQuery = new UserVideoSeriesVideoQuery();
        videoQuery.setSeriesId(seriesId);
        videoQuery.setUserId(userId);
        userVideoSeriesVideoMapper.deleteByParam(videoQuery);
    }

    @Override
    public void changeVideoSeriesSort(String userId, String seriesIds) {
        String[] seriesIdArray = seriesIds.split(",");
        List<UserVideoSeries> userVideoSeriesList = new ArrayList<>();
        Integer sort = 0;
        for (String seriesId : seriesIdArray) {
            UserVideoSeries userVideoSeries = new UserVideoSeries();
            userVideoSeries.setSeriesId(Integer.parseInt(seriesId));
            userVideoSeries.setSort(++sort);
            userVideoSeries.setUserId(userId);
            userVideoSeriesList.add(userVideoSeries);
        }
        userVideoSeriesMapper.changeSort(userVideoSeriesList);
    }

    @Override
    public List<UserVideoSeries> findListWithVideoList(UserVideoSeriesQuery query) {
        return userVideoSeriesMapper.selectListWithVideoList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserVideoSeries(UserVideoSeries userVideoSeries, String videoIds) {
        if (userVideoSeries.getSeriesId() == null && (videoIds == null || videoIds.isEmpty())){
            throw new BaseException(600,"合集名不能为空");
        }
        if (userVideoSeries.getSeriesId() == null){
            this.checkVideoIds(userVideoSeries.getUserId(), videoIds);

            userVideoSeries.setUpdateTime(new Date());
            userVideoSeries.setSort(userVideoSeriesMapper.selectMaxSort(userVideoSeries.getUserId())+1);
            log.info("保存视频合集{}", userVideoSeries);
            userVideoSeriesMapper.insert(userVideoSeries);

            this.saveSeriesVideo(userVideoSeries.getUserId(),userVideoSeries.getSeriesId(),videoIds);
        }else{
            UserVideoSeriesQuery seriesVideoQuery = new UserVideoSeriesQuery();
            seriesVideoQuery.setUserId(userVideoSeries.getUserId());
            seriesVideoQuery.setSeriesId(userVideoSeries.getSeriesId());
        }
    }

    @Override
    public void saveSeriesVideo(String userId, Integer seriesId, String videoIds) {
        UserVideoSeries userVideoSeries = getUserVideoSeriesBySeriesId(seriesId);
        if (!userVideoSeries.getUserId().equals(userId)){
            throw new BaseException(600,"不是自己的合集");//错误点
        }
        if (seriesId!= null && userVideoSeries == null){
            throw new BaseException(600,"不存在合集");
        }
        this.checkVideoIds(userId, videoIds);
        String[] videoIdArray = videoIds.split(",");
        Integer sort = userVideoSeriesVideoMapper.selectMaxSort(seriesId);
        List<UserVideoSeriesVideo> userVideoSeriesVideoList = new ArrayList<>();
        for (String videoId : videoIdArray) {
            UserVideoSeriesVideo userVideoSeriesVideo = new UserVideoSeriesVideo();
            userVideoSeriesVideo.setSeriesId(seriesId);
            userVideoSeriesVideo.setVideoId(videoId);
            userVideoSeriesVideo.setSort(++sort);
            userVideoSeriesVideo.setUserId(userId);
            userVideoSeriesVideoList.add(userVideoSeriesVideo);
        }
        userVideoSeriesVideoMapper.insert(userVideoSeriesVideoList);
    }

    private void checkVideoIds(String userId, String VideoIds){
        String[] videoIdArray = VideoIds.split(",");
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoIdArray(videoIdArray);
        videoInfoQuery.setUserId(userId);
        Integer count = videoInfoMapper.selectCount(videoInfoQuery);
        if(videoIdArray.length != count){
            throw new BaseException(600,"错误3");
        }
    }

}
