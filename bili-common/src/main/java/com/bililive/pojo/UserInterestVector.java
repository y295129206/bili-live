package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 用户兴趣向量
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterestVector implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 兴趣向量
     */
    private double[] interestVector;

    /**
     * 行为总数
     */
    private Integer behaviorCount;

    /**
     * 分类权重
     */
    private Map<String, Double> categoryWeights;

    /**
     * 标签权重
     */
    private Map<String, Double> tagWeights;
}
