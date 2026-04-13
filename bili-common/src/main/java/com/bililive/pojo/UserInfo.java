package com.bililive.pojo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 用户信息表
* @TableName user_info
*/
@Data
public class UserInfo implements Serializable {

    /**
    * 用户id
    */
    @NotBlank(message="[用户id]不能为空")
    @Size(max= 10,message="编码长度不能超过10")
    @ApiModelProperty("用户id")
    @Length(max= 10,message="编码长度不能超过10")
    private String userId;
    /**
    * 昵称
    */
    @NotBlank(message="[昵称]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("昵称")
    @Length(max= 20,message="编码长度不能超过20")
    private String nickName;
    /**
    * 邮箱
    */
    @NotBlank(message="[邮箱]不能为空")
    @Size(max= 150,message="编码长度不能超过150")
    @ApiModelProperty("邮箱")
    @Length(max= 150,message="编码长度不能超过150")
    private String email;
    /**
    * 密码
    */
    @NotBlank(message="[密码]不能为空")
    @Size(max= 50,message="编码长度不能超过50")
    @ApiModelProperty("密码")
    @Length(max= 50,message="编码长度不能超过50")
    private String password;
    /**
    * 0女1男2未知
    */
    @ApiModelProperty("0女1男2未知")
    private Integer sex;
    /**
    * 出生日期
    */
    @Size(max= 10,message="编码长度不能超过10")
    @ApiModelProperty("出生日期")
    @Length(max= 10,message="编码长度不能超过10")
    private String birthday;
    /**
    * 学校
    */
    @Size(max= 150,message="编码长度不能超过150")
    @ApiModelProperty("学校")
    @Length(max= 150,message="编码长度不能超过150")
    private String school;
    /**
    * 个人简介
    */
    @Size(max= 200,message="编码长度不能超过200")
    @ApiModelProperty("个人简介")
    @Length(max= 200,message="编码长度不能超过200")
    private String personIntroduce;
    /**
    * 加入时间
    */
    @NotNull(message="[加入时间]不能为空")
    @ApiModelProperty("加入时间")
    private Date joinTime;
    /**
    * 最后登录时间
    */
    @ApiModelProperty("最后登录时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;
    /**
    * 最后登录IP
    */
    @Size(max= 15,message="编码长度不能超过15")
    @ApiModelProperty("最后登录IP")
    @Length(max= 15,message="编码长度不能超过15")
    private String lastLoginIp;
    /**
    * 0禁用1正常
    */
    @NotNull(message="[0禁用1正常]不能为空")
    @ApiModelProperty("0禁用1正常")
    private Integer status;
    /**
    * 空间公告
    */
    @Size(max= 300,message="编码长度不能超过300")
    @ApiModelProperty("空间公告")
    @Length(max= 300,message="编码长度不能超过300")
    private String noticeInfo;
    /**
    * 硬币总数量
    */
    @NotNull(message="[硬币总数量]不能为空")
    @ApiModelProperty("硬币总数量")
    private Integer totalCoinCount;
    /**
    * 当前硬币数
    */
    @NotNull(message="[当前硬币数]不能为空")
    @ApiModelProperty("当前硬币数")
    private Integer currentCoinCount;
    /**
    * 主题
    */
    @NotNull(message="[主题]不能为空")
    @ApiModelProperty("主题")
    private Integer theme;

    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("粉丝数")
    private Integer fansCount;

    @ApiModelProperty("关注数")
    private Integer focusCount;

    @ApiModelProperty("播放数")
    private Integer playCount;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("是否关注")
    private Boolean haveFocus;
}
