package com.bililive.service.impl;

import com.bililive.Enum.StatisticsTypeEnum;
import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.mapper.StatisticsInfoMapper;
import com.bililive.mapper.UserFocusMapper;
import com.bililive.mapper.UserInfoMapper;
import com.bililive.mapper.VideoInfoMapper;
import com.bililive.pojo.*;
import com.bililive.service.StatisticsInfoService;
import com.bililive.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现类
 */
@Service
@Slf4j
public class StatisticsInfoServiceImpl implements StatisticsInfoService {

    @Autowired
    private StatisticsInfoMapper statisticsInfoMapper;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private VideoInfoMapper videoInfoMapper;
    @Autowired
    private UserFocusMapper userFocusMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public void statisticsData() {
        List<StatisticsInfo> statisticsInfoList = new ArrayList<>();
        final String statisticsDate = DateUtils.getBeforDayDate(1);
        final String statisticsDate2 = DateUtils.getBeforDayDate2(1);
        Map<String, Integer> videoPlayCountMap = redisComponent.getVideoPlayCount(statisticsDate);
        List<String> playCountKeyList= new ArrayList<>(videoPlayCountMap.keySet());
        playCountKeyList = playCountKeyList.stream().map(key -> key.substring(key.lastIndexOf(":") + 1)).collect(Collectors.toList());

        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoIdArray(playCountKeyList.toArray(new String[playCountKeyList.size()]));
        List<VideoInfo>videoInfoList = videoInfoMapper.selectList(videoInfoQuery);

        Map<String,Integer> videoCountMap = videoInfoList.stream().collect(Collectors.groupingBy(VideoInfo::getUserId,
                Collectors.summingInt(item -> videoPlayCountMap.get(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + statisticsDate + ":" + item.getVideoId()))));

        videoCountMap.forEach((userId,count)->{
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setUserId(userId);
            statisticsInfo.setStatisticsDate(statisticsDate2);
            statisticsInfo.setDataType(StatisticsTypeEnum.PLAY.getType());
            statisticsInfo.setStatisticsCount(count);
            statisticsInfoList.add(statisticsInfo);
        });

        List<StatisticsInfo> fansDataList = statisticsInfoMapper.selectStatisticsFans(statisticsDate2);
        for(StatisticsInfo statisticsInfo:fansDataList){
            statisticsInfo.setDataType(StatisticsTypeEnum.FANS.getType());
            statisticsInfo.setStatisticsDate(statisticsDate2);
        }
        statisticsInfoList.addAll(fansDataList);

        List<StatisticsInfo> commentDataList = statisticsInfoMapper.selectStatisticsComment(statisticsDate2);
        for(StatisticsInfo statisticsInfo:commentDataList){
            statisticsInfo.setDataType(StatisticsTypeEnum.COMMENT.getType());
            statisticsInfo.setStatisticsDate(statisticsDate2);
        }
        statisticsInfoList.addAll(commentDataList);

        List<StatisticsInfo> statisticsOthers = statisticsInfoMapper.selectStatisticsInfo(statisticsDate2,new Integer[]{
                UserActionTypeEnum.VIDEO_LIKE.getType(),
                UserActionTypeEnum.VIDEO_COLLECT.getType(),
                UserActionTypeEnum.VIDEO_COIN.getType(),
        });
        for(StatisticsInfo statisticsInfo:statisticsOthers){
            statisticsInfo.setStatisticsDate(statisticsDate2);
            if(UserActionTypeEnum.VIDEO_LIKE.getType().equals(statisticsInfo.getDataType())){
                statisticsInfo.setDataType(StatisticsTypeEnum.LIKE.getType());
            }
            if(UserActionTypeEnum.VIDEO_COLLECT.getType().equals(statisticsInfo.getDataType())){
                statisticsInfo.setDataType(StatisticsTypeEnum.COLLECTION.getType());
            }
            if(UserActionTypeEnum.VIDEO_COIN.getType().equals(statisticsInfo.getDataType())){
                statisticsInfo.setDataType(StatisticsTypeEnum.COIN.getType());
            }
        }
        statisticsInfoList.addAll(statisticsOthers);
        statisticsInfoMapper.insertBatch(statisticsInfoList);
    }

    @Override
    public List<StatisticsInfo> findListByParam(StatisticsInfoQuery query) {

        return statisticsInfoMapper.selectList(query);
    }

    @Override
    public Map<String, Integer> getStatisticsInfoActualTime(String userId) {
        Map<String,Integer> result = statisticsInfoMapper.selectTotalCount(userId);
        // 确保返回的Map不为null
        if (result == null) {
            result = new HashMap<>();
        }
        // 添加用户关注数
        if (userId != null && !userId.isEmpty()) {
            result.put("userCount", userFocusMapper.selectFansCount(userId));
        } else {
            result.put("userCount", userInfoMapper.selectCount(new UserInfoQuery()));
        }
        return result;
    }

    @Override
    public List<StatisticsInfo> findUserCountTotalByParam(StatisticsInfoQuery query) {
        return statisticsInfoMapper.selectListOnlyUser(query);
    }


}
