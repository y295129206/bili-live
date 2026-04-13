package com.bililive.mapper;

import com.bililive.pojo.StatisticsInfo;
import com.bililive.pojo.StatisticsInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 数据统计 Mapper 接口
 */
@Mapper
public interface StatisticsInfoMapper {

    /**
     * 插入统计数据
     */
    void insert(StatisticsInfo statisticsInfo);

    List<StatisticsInfo> selectStatisticsFans(@Param("statisticsDate") String statisticsDate);

    List<StatisticsInfo> selectStatisticsComment(@Param("statisticsDate") String statisticsDate);

    List<StatisticsInfo> selectStatisticsDanmu(@Param("statisticsDate") String statisticsDate);

    List<StatisticsInfo> selectStatisticsInfo(@Param("statisticsDate") String statisticsDate,@Param("actionTypeArray") Integer[] actionType);

    void insertBatch(List<StatisticsInfo> statisticsInfoList);

    /**
     * 根据查询条件获取统计数据列表
     * @param query 查询参数
     * @return 统计数据列表
     */
    List<StatisticsInfo> selectList(@Param("query") StatisticsInfoQuery query);

    /**
     * 查询统计总数信息
     * @param userId 用户ID（可为null）
     * @return 统计总数Map
     */
    Map<String, Integer> selectTotalCount(@Param("userId") String userId);

    List<StatisticsInfo> selectListOnlyUser(@Param("query") StatisticsInfoQuery query);


}
