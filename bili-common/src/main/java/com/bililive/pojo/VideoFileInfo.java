package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 视频文件信息
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VideoFileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件 ID
     */
    private String fileId;

    /**
     * 上传 ID（临时）
     */
    private String uploadId;

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 文件索引
     */
    private Integer fileIndex;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小（bytes）
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 更新类型：0-无更新，1-有更新
     */
    private Integer updateType;

    /**
     * 转码结果：1-转码成功，2-转码失败
     */
    private Integer transferResult;

    /**
     * 文件时长（秒）
     */
    private Integer duration;

    /**
     * 创建时间
     */
    private java.util.Date createTime;

    /**
     * 更新时间
     */
    private java.util.Date lastUpdateTime;

}
