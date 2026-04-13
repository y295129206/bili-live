package com.bililive.mapper;

import com.bililive.pojo.UserVideoSeriesVideo;
import com.bililive.pojo.UserVideoSeriesVideoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户视频合集-视频关联 Mapper 接口
 */
@Mapper
public interface UserVideoSeriesVideoMapper {

    /**
     * 插入合集视频关联
     * @param userVideoSeriesVideo 合集视频关联对象
     */
    void insert(List<UserVideoSeriesVideo> userVideoSeriesVideo);

    /**
     * 根据合集 ID 和视频 ID 查询关联
     * @param seriesId 合集 ID
     * @param videoId 视频 ID
     * @return 合集视频关联
     */
    UserVideoSeriesVideo selectBySeriesIdAndVideoId(@Param("seriesId") Integer seriesId, 
                                                     @Param("videoId") String videoId);

    /**
     * 根据合集 ID 查询视频列表
     * @param seriesId 合集 ID
     * @return 视频列表
     */
    List<UserVideoSeriesVideo> selectListBySeriesId(@Param("seriesId") Integer seriesId);

    /**
     * 根据用户 ID 和合集 ID 查询视频列表
     * @param userId 用户 ID
     * @param seriesId 合集 ID
     * @return 视频列表
     */
    List<UserVideoSeriesVideo> selectListByUserIdAndSeriesId(@Param("userId") String userId, 
                                                              @Param("seriesId") Integer seriesId);

    /**
     * 删除合集视频关联
     * @param seriesId 合集 ID
     * @param videoId 视频 ID
     */
    void deleteBySeriesIdAndVideoId(@Param("seriesId") Integer seriesId, 
                                    @Param("videoId") String videoId);

    /**
     * 根据合集 ID 删除所有关联
     * @param seriesId 合集 ID
     */
    void deleteBySeriesId(@Param("seriesId") Integer seriesId);

    /**
     * 更新排序
     * @param userVideoSeriesVideo 合集视频关联对象
     */
    void updateSort(UserVideoSeriesVideo userVideoSeriesVideo);

    /**
     * 查询最大排序值
     * @param seriesId 合集 ID
     * @return 最大排序值
     */
    Integer selectMaxSort(@Param("seriesId") Integer seriesId);

    /**
     * 根据查询条件获取列表
     * @param query 查询参数
     * @return 列表
     */
    List<UserVideoSeriesVideo> selectList(@Param("query") UserVideoSeriesVideoQuery query);

    void deleteByParam(@Param("query") UserVideoSeriesVideoQuery query);
}
