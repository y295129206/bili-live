package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频信息查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoQuery implements Serializable {

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
     * 视频 ID (支持模糊查询)
     */
    private String[] videoIdArray;

    /**
     * 用户 ID (精确匹配)
     */
    private String userId;

    /**
     * 视频标题 (支持模糊查询)
     */
    private String videoNameFuzzy;

    /**
     * 视频封面 (精确匹配)
     */
    private String videoCover;

    /**
     * 视频简介 (支持模糊查询)
     */
    private String introductionFuzzy;

    /**
     * 视频时长 (精确匹配，单位：秒)
     */
    private Integer duration;

    /**
     * 最小视频时长 (用于范围查询)
     */
    private Integer minDuration;

    /**
     * 最大视频时长 (用于范围查询)
     */
    private Integer maxDuration;

    /**
     * 视频标签 (支持模糊查询，逗号分隔)
     */
    private String tagsFuzzy;

    /**
     * 一级分类 ID (精确匹配)
     */
    private Integer pCategoryId;

    /**
     * 二级分类 ID (精确匹配)
     */
    private Integer categoryId;

    /**
     * 视频类型：0-自制，1-转载 (精确匹配)
     */
    private Integer postType;

    /**
     * 转载来源信息 (支持模糊查询)
     */
    private String originInfoFuzzy;

    /**
     * 互动设置 (支持模糊查询，逗号分隔)
     */
    private String interactionFuzzy;

    /**
     * 最后播放时间 (精确匹配)
     */
    private Date lastPlayTime;

    /**
     * 最后播放时间起始值 (用于范围查询)
     */
    private Date lastPlayTimeStart;

    /**
     * 最后播放时间结束值 (用于范围查询)
     */
    private Date lastPlayTimeEnd;

    /**
     * 推荐类型：0-未推荐，1-已推荐 (精确匹配)
     */
    private Integer recommendType;

    /**
     * 播放次数 (精确匹配)
     */
    private Integer playCount;

    /**
     * 最小播放次数 (用于范围查询)
     */
    private Integer minPlayCount;

    /**
     * 最大播放次数 (用于范围查询)
     */
    private Integer maxPlayCount;

    /**
     * 点赞次数 (精确匹配)
     */
    private Integer likeCount;

    /**
     * 投币次数 (精确匹配)
     */
    private Integer coinCount;

    /**
     * 收藏次数 (精确匹配)
     */
    private Integer collectCount;

    /**
     * 评论次数 (精确匹配)
     */
    private Integer commentCount;

    /**
     * 弹幕次数 (精确匹配)
     */
    private Integer danmuCount;

    /**
     * 创建时间 (精确匹配)
     */
    private Date createTime;

    /**
     * 创建时间起始值 (用于范围查询)
     */
    private Date createTimeStart;

    /**
     * 创建时间结束值 (用于范围查询)
     */
    private Date createTimeEnd;

    /**
     * 更新时间 (精确匹配)
     */
    private Date lastUpdateTime;

    /**
     * 更新时间起始值 (用于范围查询)
     */
    private Date lastUpdateTimeStart;

    /**
     * 更新时间结束值 (用于范围查询)
     */
    private Date lastUpdateTimeEnd;

    /**
     * 排序字段 (动态排序用，例如 "create_time", "duration", "play_count")
     */
    private String orderBy;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

    private Boolean queryUserInfo;

    private String[] excludeVideoIdArray;

    private Integer lastPlayHour;

    private Integer categoryIdOrPCategoryId;

}
