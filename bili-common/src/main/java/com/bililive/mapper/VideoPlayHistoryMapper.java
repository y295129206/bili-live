package com.bililive.mapper;

import com.bililive.pojo.VideoPlayHistory;
import com.bililive.pojo.VideoPlayHistoryQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoPlayHistoryMapper {

    /**
     * 保存或更新播放历史
     * @param history 播放历史记录
     */
    void saveOrUpdate(VideoPlayHistory history);

    /**
     * 根据用户ID查询播放历史列表
     * @param userId 用户ID
     * @return 播放历史列表
     */
    List<VideoPlayHistory> selectListByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和视频ID查询播放历史
     * @param userId 用户ID
     * @param videoId 视频ID
     * @return 播放历史记录
     */
    VideoPlayHistory selectByUserIdAndVideoId(@Param("userId") String userId, @Param("videoId") String videoId);

    /**
     * 删除用户的播放历史
     * @param userId 用户ID
     * @param videoId 视频ID
     */
    void deleteByUserIdAndVideoId(@Param("userId") String userId, @Param("videoId") String videoId);

    /**
     * 根据查询条件分页查询播放历史列表
     * @param query 查询条件
     * @return 播放历史列表
     */
    List<VideoPlayHistory> selectList(@Param("query") VideoPlayHistoryQuery query);

    /**
     * 根据查询条件统计总数
     * @param query 查询条件
     * @return 总数
     */
    Integer selectCount(@Param("query") VideoPlayHistoryQuery query);

    /**
     * 根据查询条件删除播放历史
     * @param query 查询条件
     */
    void deleteByParam(@Param("query") VideoPlayHistoryQuery query);
}
