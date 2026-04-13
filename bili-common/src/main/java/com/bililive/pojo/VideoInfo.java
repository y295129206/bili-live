package com.bililive.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频信息实体类
 */
@Data
public class VideoInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 视频标题
     */
    private String videoName;

    /**
     * 视频封面
     */
    private String videoCover;

    /**
     * 视频简介
     */
    private String introduction;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 视频标签（逗号分隔）
     */
    private String tags;

    /**
     * 一级分类 ID
     */
    private Integer pCategoryId;

    /**
     * 二级分类 ID
     */
    private Integer categoryId;

    /**
     * 视频类型：0-自制，1-转载
     */
    private Integer postType;

    /**
     * 转载来源信息
     */
    private String originInfo;

    /**
     * 互动设置（逗号分隔：0-关闭评论，1-关闭弹幕）
     */
    private String interaction;

    /**
     * 最后播放时间
     */
    private Date lastPlayTime;

    /**
     * 推荐类型：0-未推荐，1-已推荐
     */
    private Integer recommendType =0;

    /**
     * 播放次数
     */
    private Integer playCount =0;

    /**
     * 点赞次数
     */
    private Integer likeCount =0;

    /**
     * 投币次数
     */
    private Integer coinCount =0;

    /**
     * 收藏次数
     */
    private Integer collectCount =0;

    /**
     * 评论次数
     */
    private Integer commentCount =0;

    /**
     * 弹幕次数
     */
    private Integer danmuCount =0;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;
    /**
     * 用户信息
     */
    private String nickName;
    private String avatar;
}
