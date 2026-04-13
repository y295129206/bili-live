package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频弹幕查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDanmuQuery implements Serializable {

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
     * 弹幕 ID (精确匹配)
     */
    private String danmuId;

    /**
     * 视频 ID (精确匹配)
     */
    private String videoId;

    /**
     * 文件 ID (精确匹配，用于查询特定视频切片的弹幕)
     */
    private String fileId;

    /**
     * 用户 ID (精确匹配)
     */
    private String userId;

    /**
     * 弹幕内容 (支持模糊查询)
     */
    private String textFuzzy;

    /**
     * 弹幕模式 (精确匹配，0-滚动，1-顶部，2-底部，3-逆向)
     */
    private Integer mode;

    /**
     * 弹幕颜色 (精确匹配)
     */
    private String color;

    /**
     * 弹幕出现时间 (精确匹配，秒)
     */
    private Integer time;

    /**
     * 最小弹幕出现时间 (用于范围查询)
     */
    private Integer minTime;

    /**
     * 最大弹幕出现时间 (用于范围查询)
     */
    private Integer maxTime;

    /**
     * 发布时间 (精确匹配)
     */
    private Date postTime;

    /**
     * 发布时间起始值 (用于范围查询)
     */
    private Date postTimeStart;

    /**
     * 发布时间结束值 (用于范围查询)
     */
    private Date postTimeEnd;

    /**
     * 排序字段 (动态排序用，例如 "danmu_id", "time", "post_time")
     */
    private String orderBy;

    /**
     * 分页参数 (用于 MyBatis)
     */
    private SimplePage simplePage;

    /**
     * 是否查询视频信息
     */
    private Boolean queryVideoInfo;

    /**
     * 视频用户 ID (用于查询某用户发布的视频的弹幕)
     */
    private String videoUserId;

    private String videoNameFuzzy;
}
