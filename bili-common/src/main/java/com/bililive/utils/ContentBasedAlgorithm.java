package com.bililive.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于内容的推荐算法实现
 */
@Slf4j
@Component
public class ContentBasedAlgorithm {

    private static final int CATEGORY_DIM = 50;
    private static final int TAG_DIM = 100;
    private static final int TOTAL_DIM = CATEGORY_DIM + TAG_DIM;

    /**
     * 构建视频特征向量
     * @param categoryId 二级分类ID
     * @param pCategoryId 一级分类ID
     * @param tags 标签集合
     * @param title 标题
     * @return 特征向量
     */
    public double[] buildVideoFeatureVector(
            Integer categoryId,
            Integer pCategoryId,
            Set<String> tags,
            String title) {

        double[] vector = new double[TOTAL_DIM];

        // 分类特征（前50维）
        if (categoryId != null) {
            int index = categoryId % CATEGORY_DIM;
            vector[index] = 1.0;
        }

        if (pCategoryId != null) {
            int index = pCategoryId % CATEGORY_DIM;
            vector[index] += 0.5;
        }

        // 标签特征（后100维）
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                int hash = Math.abs(tag.hashCode());
                int index = CATEGORY_DIM + (hash % TAG_DIM);
                vector[index] += 1.0;
            }
        }

        // 标题关键词特征
        if (title != null && !title.isEmpty()) {
            String[] keywords = extractKeywords(title);
            for (String keyword : keywords) {
                int hash = Math.abs(keyword.hashCode());
                int index = CATEGORY_DIM + (hash % TAG_DIM);
                vector[index] += 0.5;
            }
        }

        // 归一化
        double norm = calculateNorm(vector);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    /**
     * 构建用户兴趣向量
     * @param userBehaviors 用户行为列表
     * @return 用户兴趣向量
     */
    public double[] buildUserInterestVector(List<Map<String, Object>> userBehaviors) {
        double[] interestVector = new double[TOTAL_DIM];

        if (userBehaviors == null || userBehaviors.isEmpty()) {
            return interestVector;
        }

        // 累加每个行为的权重
        for (Map<String, Object> behavior : userBehaviors) {
            Integer categoryId = (Integer) behavior.get("categoryId");
            Integer pCategoryId = (Integer) behavior.get("pCategoryId");
            Set<String> tags = (Set<String>) behavior.get("tags");
            Integer actionType = (Integer) behavior.get("actionType");

            double weight = CollaborativeFilteringAlgorithm.getActionWeight(actionType);

            // 分类维度
            if (categoryId != null) {
                int index = categoryId % CATEGORY_DIM;
                interestVector[index] += weight;
            }

            if (pCategoryId != null) {
                int index = pCategoryId % CATEGORY_DIM;
                interestVector[index] += weight * 0.5;
            }

            // 标签维度
            if (tags != null) {
                for (String tag : tags) {
                    int hash = Math.abs(tag.hashCode());
                    int index = CATEGORY_DIM + (hash % TAG_DIM);
                    interestVector[index] += weight;
                }
            }
        }

        // 归一化
        double norm = calculateNorm(interestVector);
        if (norm > 0) {
            for (int i = 0; i < interestVector.length; i++) {
                interestVector[i] /= norm;
            }
        }

        return interestVector;
    }

    /**
     * 计算用户兴趣与视频特征的相似度
     * @param userInterestVector 用户兴趣向量
     * @param videoFeatureVector 视频特征向量
     * @return 相似度得分
     */
    public double calculateRecommendationScore(double[] userInterestVector, double[] videoFeatureVector) {
        return SimilarityCalculator.calculateCosineSimilarity(userInterestVector, videoFeatureVector);
    }

    /**
     * 基于内容的推荐
     * @param userInterestVector 用户兴趣向量
     * @param videoFeatures 视频特征映射 {videoId: featureVector}
     * @param watchedVideoIds 已观看视频ID集合
     * @param topN 推荐数量
     * @return 推荐的视频ID列表（按得分排序）
     */
    public List<Map.Entry<String, Double>> recommendByContent(
            double[] userInterestVector,
            Map<String, double[]> videoFeatures,
            Set<String> watchedVideoIds,
            int topN) {

        Map<String, Double> videoScores = new HashMap<>();

        // 计算每个未观看视频的推荐得分
        for (Map.Entry<String, double[]> entry : videoFeatures.entrySet()) {
            String videoId = entry.getKey();

            if (watchedVideoIds.contains(videoId)) {
                continue;
            }

            double score = calculateRecommendationScore(userInterestVector, entry.getValue());
            if (score > 0) {
                videoScores.put(videoId, score);
            }
        }

        // 按得分降序排序，取Top-N
        return videoScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 提取标题关键词（简化版：按空格和标点分割）
     * @param title 标题
     * @return 关键词数组
     */
    private String[] extractKeywords(String title) {
        if (title == null || title.isEmpty()) {
            return new String[0];
        }

        // 移除常见停用词和标点符号
        String cleaned = title.replaceAll("[，。！？、；：\"'（）【】《》\\s]", " ");
        return cleaned.split("\\s+");
    }

    /**
     * 计算向量的范数
     * @param vector 向量
     * @return 范数
     */
    private double calculateNorm(double[] vector) {
        double sum = 0.0;
        for (double v : vector) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }

    /**
     * 基于分类的冷启动推荐
     * @param categoryIds 感兴趣的分类ID列表
     * @param allVideos 所有视频列表
     * @param topN 推荐数量
     * @return 推荐的视频ID列表
     */
    public List<String> recommendByCategory(
            List<Integer> categoryIds,
            List<Map<String, Object>> allVideos,
            int topN) {

        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 筛选匹配分类的视频
        List<Map<String, Object>> matchedVideos = allVideos.stream()
                .filter(video -> {
                    Integer categoryId = (Integer) video.get("categoryId");
                    Integer pCategoryId = (Integer) video.get("pCategoryId");
                    return categoryIds.contains(categoryId) || categoryIds.contains(pCategoryId);
                })
                .sorted((v1, v2) -> {
                    // 按播放量排序
                    Integer playCount1 = (Integer) v1.getOrDefault("playCount", 0);
                    Integer playCount2 = (Integer) v2.getOrDefault("playCount", 0);
                    return playCount2.compareTo(playCount1);
                })
                .limit(topN)
                .collect(Collectors.toList());

        return matchedVideos.stream()
                .map(video -> (String) video.get("videoId"))
                .collect(Collectors.toList());
    }
}
