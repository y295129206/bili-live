package com.bililive.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * 视频文件信息查询条件封装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoFileQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码 (默认第 1 页)
     */
    private Integer pageNum = 1;

    /**
     * 每页显示条数 (默认 10 条)
     */
    private Integer pageSize = 10;

    /**
     * 视频 ID
     */
    private String videoId;

    /**
     * 文件索引
     */
    private Integer fileIndex;

    /**
     * 上传 ID
     */
    private String uploadId;

    /**
     * 文件名称 (支持模糊查询)
     */
    private String fileName;

    /**
     * 排序字段 (动态排序用，例如 "file_index", "duration")
     */
    private String orderBy;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 转码结果
     */
    private Integer transferResult;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

}
