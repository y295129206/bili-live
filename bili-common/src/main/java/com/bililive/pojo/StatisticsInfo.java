package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据统计实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计日期
     */
    private String statisticsDate;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 数据统计类型
     */
    private Integer dataType;

    /**
     * 统计数量
     */
    private Integer statisticsCount;
}
