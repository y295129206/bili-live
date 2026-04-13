package com.bililive.pojo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 视频序列关联表
 * @TableName user_video_series_video
 */
@Data
public class UserVideoSeriesVideo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 列表 ID
     */
    @NotNull(message = "[列表 ID]不能为空")
    @ApiModelProperty("列表 ID")
    private Integer seriesId;

    /**
     * 视频 ID
     */
    @NotBlank(message = "[视频 ID]不能为空")
    @Size(max = 10, message = "编码长度不能超过 10")
    @ApiModelProperty("视频 ID")
    private String videoId;

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
     * 视频封面
     */
    @NotBlank(message = "[视频封面]不能为空")
    private String videoCover;

    /**
     * 视频名称
     */
    @NotBlank(message = "[视频名称]不能为空")
    private String videoName;
    /**
     * 播放次数
     */
    @NotNull(message = "[播放次数]不能为空")
    private Integer playCount;
    /**
     * 创建时间
     */
    @NotNull(message = "[创建时间]不能为空")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
