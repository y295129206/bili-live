package com.bililive.pojo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户视频序列归档表
 * @TableName user_video_series
 */
@Data
public class UserVideoSeries implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 列表 ID
     */
    @ApiModelProperty("列表 ID")
    private Integer seriesId;

    /**
     * 列表名称
     */
    @NotBlank(message = "[列表名称]不能为空")
    @Size(max = 100, message = "编码长度不能超过 100")
    @ApiModelProperty("列表名称")
    private String seriesName;

    /**
     * 描述
     */
    @Size(max = 200, message = "编码长度不能超过 200")
    @ApiModelProperty("描述")
    private String seriesDescription;

    /**
     * 用户 ID
     */
    @NotBlank(message = "[用户 ID]不能为空")
    @Size(max = 10, message = "编码长度不能超过 10")
    @ApiModelProperty("用户 ID")
    private String userId;

    /**
     * 排序
     */
    @NotNull(message = "[排序]不能为空")
    @ApiModelProperty("排序")
    private Integer sort;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 封面
     */
    @Size(max = 100, message = "编码长度不能超过 100")
    private String cover;

    /**
     * 视频列表
     */
    @ApiModelProperty("视频列表")
    private List<VideoInfo> videoInfoList;
}
