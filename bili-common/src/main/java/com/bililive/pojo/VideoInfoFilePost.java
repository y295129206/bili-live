package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频文件上传信息（用于前端提交）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoFilePost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件索引
     */
    private Integer fileIndex;

    /**
     * 上传 ID（临时）
     */
    private String uploadId;

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
     * 文件时长（秒）
     */
    private Integer duration;
    /**
     * 文件ID
     *  */
    private String fileId;
    /**
     * 视频ID
     *  */
    private String videoId;
    /**
     * 用户ID
     *  */
    private String userId;
    /**
     * 修改类型
     */
    private Integer updateType;
    /**
     * 转码状态
     *  */
    private  Integer transferResult;
}
