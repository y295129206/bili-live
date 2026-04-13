package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户行为查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActionQuery implements Serializable {

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
     * 用户 ID (精确匹配)
     */
    private String userId;

    /**
     * 用户 ID（模糊匹配）
     */
    private String userIdFuzzy;

    /**
     * 视频 ID (精确匹配)
     */
    private String videoId;

    /**
     * 行为类型（参考 UserActionTypeEnum）
     * 0-评论点赞，1-评论点踩，2-视频点赞，3-视频收藏，
     * 4-视频投币，5-视频评论数，6-弹幕评论数，7-视频播放数
     */
    private Integer actionType;

    /**
     * 评论 ID（精确匹配，用于查询评论相关行为）
     */
    private Integer commentId;

    /**
     * 视频作者 ID（精确匹配）
     */
    private String videoUserId;

    /**
     * 排序字段 (动态排序用，例如 "action_time", "action_count")
     */
    private String orderBy;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

    /**
     * 行为时间（精确匹配）
     */
    private String actionTime;
    private String actionTimeEnd;
    private String actionTimeStart;

    private Integer[] actionTypeArray;

    private Boolean queryVideoInfo;
}
