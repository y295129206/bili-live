package com.bililive.service;

import com.bililive.pojo.StatisticsInfo;
import com.bililive.pojo.StatisticsInfoQuery;

import java.util.List;
import java.util.Map;

/**
 * 数据统计服务接口
 */
public interface StatisticsInfoService {

    /**
     * 统计数据（每日凌晨执行）
     * 统计前一天的各项数据并保存到 statistics_info 表
     */
    void statisticsData();

    /**
     * 根据查询条件获取统计数据列表
     * @param query 查询参数
     * @return 统计数据列表
     */
    List<StatisticsInfo> findListByParam(StatisticsInfoQuery query);

    Map<String, Integer> getStatisticsInfoActualTime(String userId);

    List<StatisticsInfo> findUserCountTotalByParam(StatisticsInfoQuery query);
}
