package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 视频文件信息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一 ID
     */
    private String fileId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件索引
     */
    private Integer fileIndex;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 持续时间（秒）
     */
    private Integer duration;

}
