package com.bililive.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * 视频发布信息查询条件封装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoPostQuery implements Serializable {

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
     * 用户 ID
     */
    private String userId;

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 视频标题 (支持模糊查询)
     */
    private String videoNameFuzzy;

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
     * 视频状态：0-转码中，1-转码失败，2-待审核，3-审核通过，4-审核未通过
     */
    private Integer status;

    /**
     * 排序字段 (动态排序用，例如 "create_time", "duration", "play_count")
     */
    private String orderBy;

    /**
     * 视频时长
     */
    private Integer duration;

    /**
     * 排除视频状态列表 (逗号分隔)
     */
    private Integer[] excludeStatusArray;

    /**
     * 是否关联表
     */
    private Boolean queryCountInfo;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

    /**
     * 是否关联表
     */
    private Boolean queryUserInfo;

    /**
     * 推荐类型
     */
    private Integer recommendType;
}
