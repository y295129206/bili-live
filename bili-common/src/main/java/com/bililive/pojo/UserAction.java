package com.bililive.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户行为记录实体类
 */
@Data
public class UserAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 行为 ID
     */
    private Integer actionId;
    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 行为类型（参考 UserActionTypeEnum）
     * 0-评论点赞，1-评论点踩，2-视频点赞，3-视频收藏，
     * 4-视频投币，5-视频评论数，6-弹幕评论数，7-视频播放数
     */
    private Integer actionType;

    /**
     * 行为数量（如投币数量、点赞次数等）
     */
    private Integer actionCount = 1;

    /**
     * 评论 ID（当行为类型为评论相关时填写）
     */
    private Integer commentId;
    /**
     * 视频用户 ID
     */
    private String videoUserId;
    /**
     * 操作时间
     */
    private Date actionTime;
    /**
     * 视频名称
     */
    private String videoName;
    /**
     * 视频封面
     */
    private String videoCover;
}
