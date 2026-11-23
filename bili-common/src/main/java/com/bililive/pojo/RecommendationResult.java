package com.bililive.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 推荐结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推荐视频列表 (前端接收字段名为 list)
     */
    @JsonProperty("list")
    private List<VideoInfo> recommendList;

    /**
     * 推荐类型描述
     */
    private String recommendationType;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 生成时间戳
     */
    private Long generateTime;
}
