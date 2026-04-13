package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 视频评论查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCommentQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码 (默认第 1 页)
     */
    private Integer pageNo = 1;

    /**
     * 每页显示条数 (默认 10 条)
     */
    private Integer pageSize = 10;

    /**
     * 视频 ID (精确匹配)
     */
    private String videoId;

    /**
     * 用户 ID (精确匹配)
     */
    private String userId;

    /**
     * 评论 ID (精确匹配)
     */
    private Integer commentId;

    /**
     * 父评论 ID (精确匹配，用于查询一级评论下的回复)
     */
    private Integer pCommentId;


    private String orderBy;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

    /**
     * 是否需要查询用户信息
     */
    private Boolean queryUserInfo = false;

    /**
     * 是否需要查子评论
     */
    private Boolean loadChildren = false;

    /**
     * 置顶类型
     */
    private Integer topType;

    /**
     * 是否需要查询视频信息
     */
    private Boolean queryVideoInfo = false;

    private String videoNameFuzzy;

    private String videoUserId;
}
