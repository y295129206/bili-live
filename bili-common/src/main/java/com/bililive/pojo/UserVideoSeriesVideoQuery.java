package com.bililive.pojo;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 视频序列关联表查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVideoSeriesVideoQuery implements Serializable {

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
     * 视频 ID (精确匹配)
     */
    @ApiModelProperty("视频 ID")
    private String videoId;

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
     * 排序字段 (动态排序用，例如 "sort")
     */
    @ApiModelProperty("排序字段")
    private String orderBy;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

    private Boolean queryVideoInfo;

}
