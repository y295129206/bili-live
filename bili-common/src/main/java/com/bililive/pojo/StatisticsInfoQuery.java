package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据统计信息查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsInfoQuery implements Serializable {

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
     * 统计日期 (精确匹配，格式：yyyy-MM-dd)
     */
    private String statisticsDate;

    /**
     * 用户ID (精确匹配)
     */
    private String userId;

    /**
     * 数据统计类型 (精确匹配)
     */
    private Integer dataType;

    /**
     * 排序字段 (动态排序用，例如 "statistics_date", "statistics_count")
     */
    private String orderBy;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

    private String statisticsDateStart;

    private String statisticsDateEnd;

}
