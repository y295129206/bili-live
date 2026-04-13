package com.bililive.pojo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 视频评论表
* @TableName video_comment
*/
@Data
public class VideoComment implements Serializable {

    /**
    * 评论 ID
    */
    @ApiModelProperty("评论 ID")
    private Integer commentId;
    
    /**
    * 视频 ID
    */
    @NotBlank(message="[视频 ID]不能为空")
    @Size(max= 64,message="编码长度不能超过 64")
    @ApiModelProperty("视频 ID")
    private String videoId;
    
    /**
    * 用户 ID
    */
    @NotBlank(message="[用户 ID]不能为空")
    @Size(max= 10,message="编码长度不能超过 10")
    @ApiModelProperty("用户 ID")
    private String userId;
    
    /**
    * 用户头像
    */
    @ApiModelProperty("用户头像")
    private String avatar;
    
    /**
    * 用户昵称
    */
    @Size(max= 20,message="编码长度不能超过 20")
    @ApiModelProperty("用户昵称")
    private String nickName;
    
    /**
    * 评论内容
    */
    @NotBlank(message="[评论内容]不能为空")
    @Size(max= 500,message="编码长度不能超过 500")
    @ApiModelProperty("评论内容")
    private String content;
    
    /**
    * 评论图片路径
    */
    @Size(max= 50,message="编码长度不能超过 50")
    @ApiModelProperty("评论图片路径")
    private String imgPath;
    
    /**
    * 父评论 ID（用于回复评论）
    */
    @ApiModelProperty("父评论 ID")
    @JsonProperty("pCommentId")
    private Integer pCommentId;

    /**
    * 回复的评论用户 ID
    */
    @ApiModelProperty("回复的评论用户 ID")
    private String replyUserId;

    /**
    * 回复的评论用户昵称
    */
    @ApiModelProperty("回复的评论用户昵称")
    private String replyNickName;
    
    /**
    * 回复的评论 ID
    */
    @ApiModelProperty("回复的评论 ID")
    private Integer replyCommentId;
    
    /**
    * 点赞数
    */
    @ApiModelProperty("点赞数")
    private Integer likeCount = 0;
    
    /**
    * 点踩数
    */
    @ApiModelProperty("点踩数")
    private Integer hateCount = 0;
    
    /**
    * 评论时间
    */
    @NotNull(message="[评论时间]不能为空")
    @ApiModelProperty("评论时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date postTime;

    /**
    * 回复的评论用户头像
    */
    @ApiModelProperty("回复的评论用户头像")
    private String replyAvatar;

    /**
    * 视频用户 ID
    */
    @ApiModelProperty("视频用户 ID")
    private String videoUserId;

    /**
    * 置顶类型
    */
    @ApiModelProperty("置顶类型")
    private Integer topType = 0;

    /**
    * 子评论列表
    */
    @ApiModelProperty("子评论列表")
    private List<VideoComment> children;


    @ApiModelProperty("视频封面")
    private String videoCover;

    @ApiModelProperty("视频名称")
    private String videoName;
    
    private static final long serialVersionUID = 1L;
}
