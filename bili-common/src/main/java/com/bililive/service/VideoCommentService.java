package com.bililive.service;

import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.VideoComment;
import com.bililive.pojo.VideoCommentQuery;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 视频评论服务接口
 */
public interface VideoCommentService {

    /**
     * 发表评论
     * @param videoComment 评论对象
     */
    void postComment(VideoComment videoComment,Integer replyCommentId);

    /**
     * 根据 ID 查询评论
     * @param commentId 评论 ID
     * @return 评论记录
     */
    VideoComment getCommentByCommentId(Integer commentId);

    /**
     * 根据查询条件获取评论列表
     * @param query 查询参数
     * @return 评论列表
     */
    List<VideoComment> findListByParam(VideoCommentQuery query);

    /**
     * 分页查询评论列表
     * @param query 查询参数
     * @return 分页结果
     */
    PaginationResult<VideoComment> findListByPage(VideoCommentQuery query);


    /**
     * 删除评论
     * @param commentId 评论 ID
     */
    void deleteComment(Integer commentId, String userId);

    void topComment(@NotNull Integer commentId, String userId);

    void cancelTopComment(@NotNull Integer commentId, String userId);
}
