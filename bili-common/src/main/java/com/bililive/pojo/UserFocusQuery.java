package com.bililive.pojo;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户关注表查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFocusQuery implements Serializable {

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
    @ApiModelProperty("用户 ID")
    private String userId;

    /**
     * 关注用户 ID (精确匹配)
     */
    @ApiModelProperty("关注用户 ID")
    private String focusUserId;

    /**
     * 关注时间 (精确匹配)
     */
    @ApiModelProperty("关注时间")
    private Date focusTime;

    /**
     * 关注时间起始值 (用于范围查询)
     */
    @ApiModelProperty("关注时间起始值")
    private Date focusTimeStart;

    /**
     * 关注时间结束值 (用于范围查询)
     */
    @ApiModelProperty("关注时间结束值")
    private Date focusTimeEnd;

    /**
     * 排序字段 (动态排序用，例如 "focus_time")
     */
    @ApiModelProperty("排序字段")
    private String orderBy;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;


    private Integer queryType;

}
