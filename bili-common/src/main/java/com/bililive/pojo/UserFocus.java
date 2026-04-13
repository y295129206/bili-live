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
 * 用户关注表
 * @TableName user_focus
 */
@Data
public class UserFocus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @NotBlank(message = "[用户 ID]不能为空")
    @Size(max = 10, message = "编码长度不能超过 10")
    @ApiModelProperty("用户 ID")
    private String userId;

    /**
     * 关注用户 ID
     */
    @NotBlank(message = "[关注用户 ID]不能为空")
    @Size(max = 10, message = "编码长度不能超过 10")
    @ApiModelProperty("关注用户 ID")
    private String focusUserId;

    /**
     * 关注时间
     */
    @NotNull(message = "[关注时间]不能为空")
    @ApiModelProperty("关注时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date focusTime;

    private String otherNickName;
    private String otherAvatar;
    private String otherUserId;
    private String otherPersonIntroduction;
    private Integer focusType;

}
