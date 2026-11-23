package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 视频特征向量
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFeatureVector implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频ID
     */
    private String videoId;

    /**
     * 特征向量
     */
    private double[] featureVector;

    /**
     * 二级分类ID
     */
    private Integer categoryId;

    /**
     * 一级分类ID
     */
    private Integer pCategoryId;

    /**
     * 标签集合
     */
    private Set<String> tags;

    /**
     * 标题关键词
     */
    private String titleKeywords;
}
