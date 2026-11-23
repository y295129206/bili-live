package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 物品相似度
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSimilarity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标视频ID
     */
    private String targetVideoId;

    /**
     * 相似视频ID
     */
    private String similarVideoId;

    /**
     * 相似度分数
     */
    private Double similarityScore;

    /**
     * 共同用户数
     */
    private Integer commonUsers;
}
