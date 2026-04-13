package com.bililive.mapper;

import com.bililive.pojo.VideoComment;
import com.bililive.pojo.VideoCommentQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频评论 Mapper 接口
 */
@Mapper
public interface VideoCommentMapper {

    /**
     * 插入评论记录
     * @param videoComment 评论对象
     */
    void insert(VideoComment videoComment);

    /**
     * 根据 ID 查询评论
     * @param commentId 评论 ID
     * @return 评论记录
     */
    VideoComment selectByCommentId(@Param("commentId") Integer commentId);

    /**
     * 根据视频 ID 查询评论列表
     * @param query 查询参数
     * @return 评论列表
     */
    List<VideoComment> selectList(@Param("query") VideoCommentQuery query);

    /**
     * 根据查询条件统计评论总数
     * @param query 查询参数
     * @return 总数
     */
    Integer selectCount(@Param("query") VideoCommentQuery query);


    /**
     * 更新评论
     * @param videoComment 评论对象
     */
    void updateByCommentId(VideoComment videoComment);

    /**
     * 根据评论 ID 删除评论
     * @param commentId 评论 ID
     */
    void deleteByCommentId(@Param("commentId") Integer commentId);

    /**
     * 批量删除评论
     * @param commentIds 评论 ID 数组
     */
    void deleteBatch(@Param("commentIds") Integer[] commentIds);

    /**
     * 更新点赞数
     * @param commentId 评论 ID
     * @param changeCount 变化数量（+1 或 -1）
     */
    void updateLikeCount(@Param("commentId") Integer commentId, @Param("changeCount") Integer changeCount);

    /**
     * 更新点踩数
     * @param commentId 评论 ID
     * @param changeCount 变化数量（+1 或 -1）
     */
    void updateHateCount(@Param("commentId") Integer commentId, @Param("changeCount") Integer changeCount);

    List<VideoComment> selectListWithChildren(@Param("query") VideoCommentQuery query);

    void updateCountInfo(@Param("commentId") Integer commentId, @Param("field") String field, @Param("changeCount") Integer changeCount
                        , @Param("opposeField") String opposeField, @Param("opposeChangeCount") Integer opposeChangeCount);

    void updateByParam(VideoComment videoComment, @Param("query") VideoCommentQuery query);

    /**
     * 根据查询条件删除评论
     * @param query 查询参数
     */
    void deleteByParam(@Param("query") VideoCommentQuery query);

    /**
     * 根据视频 ID 删除评论
     * @param videoId 视频 ID
     */
    void deleteByVideoId(@Param("videoId") String videoId);
}
