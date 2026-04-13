package com.bililive.pojo;

import com.bililive.Enum.VideoStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频发布信息（用于前端提交）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoPost extends VideoInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 视频封面
     */
    private String videoCover;

    /**
     * 视频标题
     */
    private String videoName;

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
     * 视频标签（逗号分隔）
     */
    private String tags;

    /**
     * 视频简介
     */
    private String introduction;

    /**
     * 互动设置（逗号分隔：0-关闭弹幕，1-关闭评论）
     */
    private String interaction;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 视频状态：0-转码中，1-转码失败，2-待审核，3-审核通过，4-审核未通过
     */
    private Integer status;
    /**
     * 创建时间
     *  */
    private Date createTime;
    /**
     * 最后修改时间
     *  */
    private Date lastUpdateTime;

    /**
     * 视频时长
     */
    private Integer duration;

    /**
     * 用户昵称（联表查询用）
     */
    private String nickName;

    /**
     * 用户头像（联表查询用）
     */
    private String avatar;

    private String statusName;

    public String getStatusName() {
        VideoStatusEnum videoStatusEnum = VideoStatusEnum.getByStatus(this.status);
        return videoStatusEnum==null?"":videoStatusEnum.getDesc();
    }
}
