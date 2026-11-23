package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户相似度
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSimilarity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标用户ID
     */
    private String targetUserId;

    /**
     * 相似用户ID
     */
    private String similarUserId;

    /**
     * 相似度分数
     */
    private Double similarityScore;

    /**
     * 共同视频数
     */
    private Integer commonVideos;
}
