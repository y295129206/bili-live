package com.bililive.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * 视频播放历史查询条件封装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoPlayHistoryQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码 (默认第 1 页)
     */
    private Integer pageNo = 1;

    /**
     * 每页显示条数 (默认 10 条)
     */
    private Integer pageSize = 10;

    /**
     * 用户 ID (精确匹配)
     */
    private String userId;

    /**
     * 视频 ID (精确匹配)
     */
    private String videoId;

    /**
     * 排序字段 (动态排序用，例如 "last_update_time")
     */
    private String orderBy;

    /**
     * 文件索引 (精确匹配)
     */
    private Integer fileIndex;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

    private String lastUpdateTime;

    private String lastUpdateTimeStart;

    private String lastUpdateTimeEnd;

    private Boolean queryVideoDetail;


}
