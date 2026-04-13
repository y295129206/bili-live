package com.bililive.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频弹幕实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDanmu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 弹幕 ID
     */
    private String danmuId;

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 文件 ID（视频切片文件 ID）
     */
    private String fileId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 弹幕内容
     */
    private String text;

    /**
     * 弹幕模式（0-滚动，1-顶部，2-底部，3-逆向）
     */
    private Integer mode;

    /**
     * 弹幕颜色
     */
    private String color;

    /**
     * 弹幕出现时间（秒，相对于视频开始的时间点）
     */
    private Integer time;

    /**
     * 发布时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date postTime;
    /**
     * 视频名称
     */
    private String videoName;
    /**
     * 视频封面
     */
    private String videoCover;
    /**
     * 用户昵称
     */
    private String nickName;

}
