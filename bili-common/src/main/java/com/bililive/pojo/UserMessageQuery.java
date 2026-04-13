package com.bililive.pojo;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户消息查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageQuery implements Serializable {

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
     * 消息ID (精确匹配)
     */
    @ApiModelProperty("消息ID")
    private Integer messageId;

    /**
     * 用户ID (精确匹配)
     */
    @ApiModelProperty("用户ID")
    private String userId;

    /**
     * 视频ID (精确匹配)
     */
    @ApiModelProperty("视频ID")
    private String videoId;

    /**
     * 消息类型 (精确匹配)
     */
    @ApiModelProperty("消息类型")
    private Integer messageType;

    /**
     * 发送人ID (精确匹配)
     */
    @ApiModelProperty("发送人ID")
    private String sendUserId;

    /**
     * 阅读状态 0:未读 1:已读
     */
    @ApiModelProperty("阅读状态")
    private Integer readType;

    /**
     * 创建时间起始值 (用于范围查询)
     */
    @ApiModelProperty("创建时间起始值")
    private Date createTimeStart;

    /**
     * 创建时间结束值 (用于范围查询)
     */
    @ApiModelProperty("创建时间结束值")
    private Date createTimeEnd;

    /**
     * 排序字段 (动态排序用，例如 "create_time desc")
     */
    @ApiModelProperty("排序字段")
    private String orderBy;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

    /**
     * 是否查询扩展信息
     */
    private Boolean queryExtendInfo;

}
