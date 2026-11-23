package com.bililive.utils;

import com.bililive.Enum.UserActionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 协同过滤算法实现
 */
@Slf4j
@Component
public class CollaborativeFilteringAlgorithm {

    private static final int DEFAULT_K_NEIGHBORS = 10;
    private static final int BEHAVIOR_THRESHOLD = 5;

    /**
     * 行为权重配置
     */
    private static final Map<Integer, Double> ACTION_WEIGHTS = new HashMap<>();

    static {
        ACTION_WEIGHTS.put(UserActionTypeEnum.VIDEO_PLAY.getType(), 1.0);
        ACTION_WEIGHTS.put(UserActionTypeEnum.VIDEO_COMMENT.getType(), 2.0);
        ACTION_WEIGHTS.put(UserActionTypeEnum.VIDEO_DANMU.getType(), 2.0);
        ACTION_WEIGHTS.put(UserActionTypeEnum.VIDEO_LIKE.getType(), 3.0);
        ACTION_WEIGHTS.put(UserActionTypeEnum.VIDEO_COLLECT.getType(), 4.0);
        ACTION_WEIGHTS.put(UserActionTypeEnum.VIDEO_COIN.getType(), 5.0);
    }

    /**
     * 计算用户相似度（基于余弦相似度）
     * @param targetUserId 目标用户ID
     * @param userVideoRatings 用户-视频评分矩阵 {userId: {videoId: score}}
     * @return 相似用户及其相似度 {userId: similarity}
     */
    public Map<String, Double> calculateUserSimilarities(
            String targetUserId,
            Map<String, Map<String, Double>> userVideoRatings) {

        Map<String, Double> similarities = new HashMap<>();
        Map<String, Double> targetRatings = userVideoRatings.get(targetUserId);

        if (targetRatings == null || targetRatings.isEmpty()) {
            return similarities;
        }

        for (Map.Entry<String, Map<String, Double>> entry : userVideoRatings.entrySet()) {
            String otherUserId = entry.getKey();
            if (targetUserId.equals(otherUserId)) {
                continue;
            }

            Map<String, Double> otherRatings = entry.getValue();
            
            // 找出共同评分过的视频
            Set<String> commonVideos = new HashSet<>(targetRatings.keySet());
            commonVideos.retainAll(otherRatings.keySet());

            if (commonVideos.isEmpty()) {
                continue;
            }

            // 构建向量
            double[] targetVector = new double[commonVideos.size()];
            double[] otherVector = new double[commonVideos.size()];
            int index = 0;

            for (String videoId : commonVideos) {
                targetVector[index] = targetRatings.get(videoId);
                otherVector[index] = otherRatings.get(videoId);
                index++;
            }

            // 计算余弦相似度
            double similarity = SimilarityCalculator.calculateCosineSimilarity(targetVector, otherVector);

            if (similarity > 0) {
                similarities.put(otherUserId, similarity);
            }
        }

        // 按相似度降序排序，取Top-K
        return similarities.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(DEFAULT_K_NEIGHBORS)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 基于用户的协同过滤推荐
     * @param targetUserId 目标用户ID
     * @param userVideoRatings 用户-视频评分矩阵
     * @param userSimilarities 用户相似度
     * @param watchedVideoIds 已观看视频ID集合
     * @param topN 推荐数量
     * @return 推荐的视频ID列表
     */
    public List<String> recommendByUserCF(
            String targetUserId,
            Map<String, Map<String, Double>> userVideoRatings,
            Map<String, Double> userSimilarities,
            Set<String> watchedVideoIds,
            int topN) {

        Map<String, Double> videoScores = new HashMap<>();

        // 遍历每个相似用户
        for (Map.Entry<String, Double> entry : userSimilarities.entrySet()) {
            String neighborUserId = entry.getKey();
            double similarity = entry.getValue();

            Map<String, Double> neighborRatings = userVideoRatings.get(neighborUserId);
            if (neighborRatings == null) {
                continue;
            }

            // 对邻居用户喜欢的视频进行加权评分
            for (Map.Entry<String, Double> ratingEntry : neighborRatings.entrySet()) {
                String videoId = ratingEntry.getKey();

                // 跳过已观看的视频
                if (watchedVideoIds.contains(videoId)) {
                    continue;
                }

                // 加权求和：相似度 * 评分
                double score = similarity * ratingEntry.getValue();
                videoScores.merge(videoId, score, Double::sum);
            }
        }

        // 按得分降序排序，取Top-N
        return videoScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 计算物品相似度（基于Jaccard相似度）
     * @param targetVideoId 目标视频ID
     * @param videoUsers 视频-用户矩阵 {videoId: {userId}}
     * @return 相似视频及其相似度 {videoId: similarity}
     */
    public Map<String, Double> calculateItemSimilarities(
            String targetVideoId,
            Map<String, Set<String>> videoUsers) {

        Map<String, Double> similarities = new HashMap<>();
        Set<String> targetUsers = videoUsers.get(targetVideoId);

        if (targetUsers == null || targetUsers.isEmpty()) {
            return similarities;
        }

        for (Map.Entry<String, Set<String>> entry : videoUsers.entrySet()) {
            String otherVideoId = entry.getKey();
            if (targetVideoId.equals(otherVideoId)) {
                continue;
            }

            Set<String> otherUsers = entry.getValue();
            
            // 计算交集
            Set<String> commonUsers = new HashSet<>(targetUsers);
            commonUsers.retainAll(otherUsers);

            if (commonUsers.isEmpty()) {
                continue;
            }

            // Jaccard相似度 = 交集 / 并集
            double intersection = commonUsers.size();
            double union = targetUsers.size() + otherUsers.size() - intersection;
            double similarity = intersection / union;

            if (similarity > 0) {
                similarities.put(otherVideoId, similarity);
            }
        }

        // 按相似度降序排序，取Top-K
        return similarities.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(DEFAULT_K_NEIGHBORS)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 基于物品的协同过滤推荐
     * @param likedVideoIds 用户喜欢的视频ID集合
     * @param videoSimilarities 视频相似度矩阵 {videoId: {similarVideoId: similarity}}
     * @param watchedVideoIds 已观看视频ID集合
     * @param topN 推荐数量
     * @return 推荐的视频ID列表
     */
    public List<String> recommendByItemCF(
            Set<String> likedVideoIds,
            Map<String, Map<String, Double>> videoSimilarities,
            Set<String> watchedVideoIds,
            int topN) {

        Map<String, Double> videoScores = new HashMap<>();

        // 遍历用户喜欢的每个视频
        for (String likedVideoId : likedVideoIds) {
            Map<String, Double> similarities = videoSimilarities.get(likedVideoId);
            if (similarities == null) {
                continue;
            }

            // 累加相似视频的得分
            for (Map.Entry<String, Double> entry : similarities.entrySet()) {
                String videoId = entry.getKey();

                if (watchedVideoIds.contains(videoId)) {
                    continue;
                }

                videoScores.merge(videoId, entry.getValue(), Double::sum);
            }
        }

        // 按得分降序排序，取Top-N
        return videoScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 根据行为类型获取权重
     * @param actionType 行为类型
     * @return 权重值
     */
    public static double getActionWeight(Integer actionType) {
        return ACTION_WEIGHTS.getOrDefault(actionType, 1.0);
    }

    /**
     * 计算混合推荐权重系数
     * @param behaviorCount 用户行为总数
     * @return 权重系数 alpha
     */
    public static double calculateHybridWeight(int behaviorCount) {
        return (double) behaviorCount / (behaviorCount + BEHAVIOR_THRESHOLD);
    }

    /**
     * 判断是否为新用户
     * @param behaviorCount 用户行为总数
     * @return true-新用户，false-老用户
     */
    public static boolean isNewUser(int behaviorCount) {
        return behaviorCount < BEHAVIOR_THRESHOLD;
    }
}
