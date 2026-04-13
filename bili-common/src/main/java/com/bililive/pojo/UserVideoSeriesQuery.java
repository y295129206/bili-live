package com.bililive.pojo;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户视频序列归档表查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVideoSeriesQuery implements Serializable {

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
     * 列表 ID (精确匹配)
     */
    @ApiModelProperty("列表 ID")
    private Integer seriesId;

    /**
     * 列表名称 (支持模糊查询)
     */
    @ApiModelProperty("列表名称")
    private String seriesNameFuzzy;

    /**
     * 描述 (支持模糊查询)
     */
    @ApiModelProperty("描述")
    private String seriesDescriptionFuzzy;

    /**
     * 用户 ID (精确匹配)
     */
    @ApiModelProperty("用户 ID")
    private String userId;

    /**
     * 排序 (精确匹配)
     */
    @ApiModelProperty("排序")
    private Integer sort;

    /**
     * 更新时间 (精确匹配)
     */
    @ApiModelProperty("更新时间")
    private Date updateTime;

    /**
     * 更新时间起始值 (用于范围查询)
     */
    @ApiModelProperty("更新时间起始值")
    private Date updateTimeStart;

    /**
     * 更新时间结束值 (用于范围查询)
     */
    @ApiModelProperty("更新时间结束值")
    private Date updateTimeEnd;

    /**
     * 排序字段 (动态排序用，例如 "sort", "update_time")
     */
    @ApiModelProperty("排序字段")
    private String orderBy;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

}
