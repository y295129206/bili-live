package com.bililive.mapper;

import com.bililive.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频发布信息 Mapper 接口
 */
@Mapper
public interface VideoInfoPostMapper {

    /**
     * 根据视频 ID 查询视频信息
     */
    VideoInfoPost selectByVideoId(@Param("videoId") String videoId);

    /**
     * 插入视频发布信息
     */
    void insert(VideoInfoPost videoInfoPost);

    /**
     * 更新视频发布信息
     */
    void updateByVideoId(VideoInfoPost videoInfoPost, @Param("videoId") String videoId);

    /**
     * 删除视频发布信息
     */
    void deleteByVideoId(@Param("videoId") String videoId);

    /**
     * 根据用户 ID 查询视频列表
     */
    List<VideoInfoPost> selectByUserId(@Param("userId") String userId);

    /**
     * 根据状态查询视频列表
     */
    List<VideoInfoPost> selectByStatus(@Param("status") Integer status);

    /**
     * 获取视频总数
     */
    Integer selectCount();

    /**
     * 根据查询条件获取视频总数
     */
    Integer selectCountByQuery(@Param("query") VideoInfoPostQuery query);

    /**
     * 根据查询条件获取视频列表
     */
    List<VideoInfoPost> selectList(@Param("query") VideoInfoPostQuery query);

    /**
     * 根据查询条件更新视频信息
     */
    Integer updateByParam(@Param("videoInfoPost") VideoInfoPost videoInfoPost, @Param("query") VideoInfoPostQuery query);

    List<VideoInfoFile> selectListByVideoId(String videoId);
}
