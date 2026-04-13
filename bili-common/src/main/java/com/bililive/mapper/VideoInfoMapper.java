package com.bililive.mapper;

import com.bililive.pojo.UserCountInfoDto;
import com.bililive.pojo.VideoInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.bililive.pojo.VideoInfoQuery;

import java.util.List;

/**
 * 视频信息 Mapper 接口
 */
@Mapper
public interface VideoInfoMapper {

    /**
     * 根据视频 ID 查询视频信息
     */
    VideoInfo selectByVideoId(@Param("videoId") String videoId);

    /**
     * 插入视频信息
     */
    void insert(VideoInfo videoInfo);

    /**
     * 根据视频 ID 更新视频信息
     */
    void updateByVideoId(VideoInfo videoInfo, @Param("videoId") String videoId);

    /**
     * 删除视频信息
     */
    void deleteByVideoId(@Param("videoId") String videoId);

    /**
     * 插入或更新视频信息
     */
    void insertOrUpdate(VideoInfo videoInfo);

    /**
     * 根据查询条件获取视频列表
     * @param query 查询参数
     * @return 视频列表
     */
    List<VideoInfo> selectList(@Param("query") VideoInfoQuery query);

    /**
     * 根据查询条件获取视频总数
     * @param query 查询参数
     * @return 视频总数
     */
    Integer selectCount(@Param("query") VideoInfoQuery query);

    /**
     * 更新视频数量统计信息
     * @param videoId 视频 ID
     * @param field 字段名（如 like_count, collect_count 等）
     * @param changeCount 变化数量（正数增加，负数减少）
     */
    void updateCountInfo(@Param("videoId") String videoId, 
                        @Param("field") String field, 
                        @Param("changeCount") Integer changeCount);

    /**
     * 根据查询条件更新视频信息
     * @param videoInfo 要更新的视频信息
     * @param query 查询条件
     * @return 更新的记录数
     */
    Integer updateByParam(@Param("videoInfo") VideoInfo videoInfo, @Param("query") VideoInfoQuery query);

    UserCountInfoDto selectSumCountInfo(@Param("userId") String userId);
}
