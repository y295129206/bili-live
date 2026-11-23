package com.bililive.service;

import com.bililive.pojo.RecommendationResult;

/**
 * 视频推荐服务接口
 */
public interface VideoRecommendationService {

    /**
     * 获取个性化推荐列表
     * @param userId 用户ID
     * @param topN 推荐数量
     * @return 推荐结果
     */
    RecommendationResult getPersonalizedRecommendation(String userId, int topN);

    /**
     * 获取热门推荐列表
     * @param topN 推荐数量
     * @return 推荐结果
     */
    RecommendationResult getHotRecommendation(int topN);

    /**
     * 获取冷启动推荐列表（新用户）
     * @param userId 用户ID
     * @param topN 推荐数量
     * @return 推荐结果
     */
    RecommendationResult getColdStartRecommendation(String userId, int topN);

    /**
     * 清除用户推荐缓存
     * @param userId 用户ID
     */
    void clearRecommendationCache(String userId);
}
