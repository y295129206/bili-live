package com.bililive.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频信息 Elasticsearch DTO
 * 用于 Elasticsearch 搜索引擎的视频文档存储
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoEsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 视频封面
     */
    private String videoCover;

    /**
     * 视频标题（使用 ik_max_word 分词器）
     */
    private String videoName;

    /**
     * 视频标签（使用 comma 分词器，逗号分隔）
     */
    private String tags;

    /**
     * 播放次数
     */
    private Integer playCount;

    /**
     * 弹幕次数
     */
    private Integer danmuCount;

    /**
     * 收藏次数
     */
    private Integer collectCount;

    /**
     * 创建时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
