package com.bililive.service.impl;

import com.bililive.Enum.RecommendationTypeEnum;
import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.component.RedisComponent;
import com.bililive.mapper.UserActionMapper;
import com.bililive.mapper.VideoInfoMapper;
import com.bililive.pojo.*;
import com.bililive.service.VideoRecommendationService;
import com.bililive.utils.CollaborativeFilteringAlgorithm;
import com.bililive.utils.ContentBasedAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 视频推荐服务实现类
 */
@Service
@Slf4j
public class VideoRecommendationServiceImpl implements VideoRecommendationService {

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private VideoInfoMapper videoInfoMapper;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private CollaborativeFilteringAlgorithm cfAlgorithm;

    @Autowired
    private ContentBasedAlgorithm contentAlgorithm;

    private static final int CACHE_EXPIRE_SECONDS = 300; // 5分钟缓存
    private static final int BEHAVIOR_THRESHOLD = 50;
    private static final int TOP_K_NEIGHBORS = 10;
    private static final int DEFAULT_TOP_N = 10;

    @Override
    public RecommendationResult getPersonalizedRecommendation(String userId, int topN) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 尝试从缓存获取
        String cacheKey = "recommendation:" + userId + ":" + topN;
        RecommendationResult cachedResult = (RecommendationResult) redisComponent.getRedisUtils().get(cacheKey);
        if (cachedResult != null) {
            log.info("命中推荐缓存: userId={}", userId);
            return cachedResult;
        }

        // 获取用户行为数据
        List<UserAction> userActions = getUserActions(userId);
        int behaviorCount = userActions.size();

        RecommendationResult result;

        // 判断用户类型
        if (CollaborativeFilteringAlgorithm.isNewUser(behaviorCount)) {
            // 新用户：使用冷启动推荐
            log.info("新用户推荐: userId={}, behaviorCount={}", userId, behaviorCount);
            result = getColdStartRecommendation(userId, topN);
        } else {
            // 老用户：使用混合推荐
            log.info("混合推荐: userId={}, behaviorCount={}", userId, behaviorCount);
            result = getHybridRecommendation(userId, userActions, behaviorCount, topN);
        }

        // 缓存结果
        if (result != null && result.getRecommendList() != null && !result.getRecommendList().isEmpty()) {
            redisComponent.getRedisUtils().setex(cacheKey, result, CACHE_EXPIRE_SECONDS * 1000L);
        }

        return result;
    }

    @Override
    public RecommendationResult getHotRecommendation(int topN) {
        String cacheKey = "recommendation:hot:" + topN;
        RecommendationResult cachedResult = (RecommendationResult) redisComponent.getRedisUtils().get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 查询热门视频（按播放量排序）
        VideoInfoQuery query = new VideoInfoQuery();
        query.setOrderBy("play_count desc");
        query.setPageNo(1);
        query.setPageSize(topN);

        List<VideoInfo> hotVideos = videoInfoMapper.selectList(query);

        RecommendationResult result = RecommendationResult.builder()
                .recommendList(hotVideos)
                .recommendationType(RecommendationTypeEnum.HOT.getDesc())
                .confidence(1.0)
                .generateTime(System.currentTimeMillis())
                .build();

        redisComponent.getRedisUtils().setex(cacheKey, result, CACHE_EXPIRE_SECONDS * 1000L);
        return result;
    }

    @Override
    public RecommendationResult getColdStartRecommendation(String userId, int topN) {
        // 策略1：热门推荐（Top-50）
        List<VideoInfo> hotVideos = getHotRecommendation(Math.min(topN, 50)).getRecommendList();

        // 策略2：全分类均衡推荐
        List<VideoInfo> categoryBalancedVideos = getCategoryBalancedVideos(topN - hotVideos.size());

        // 合并结果
        List<VideoInfo> recommendList = new ArrayList<>();
        recommendList.addAll(hotVideos);
        recommendList.addAll(categoryBalancedVideos);

        // 去重并限制数量
        recommendList = recommendList.stream()
                .distinct()
                .limit(topN)
                .collect(Collectors.toList());

        return RecommendationResult.builder()
                .recommendList(recommendList)
                .recommendationType(RecommendationTypeEnum.COLD_START.getDesc())
                .confidence(0.5)
                .generateTime(System.currentTimeMillis())
                .build();
    }

    @Override
    public void clearRecommendationCache(String userId) {
        // 清除所有相关缓存
        Set<String> keys = redisComponent.getRedisUtils().getByKeyPrefix("recommendation:" + userId + ":");
        if (keys != null && !keys.isEmpty()) {
            redisComponent.getRedisUtils().delete(keys.toArray(new String[0]));
        }
        log.info("清除推荐缓存: userId={}", userId);
    }

    /**
     * 混合推荐（协同过滤 + 内容推荐）
     */
    private RecommendationResult getHybridRecommendation(
            String userId,
            List<UserAction> userActions,
            int behaviorCount,
            int topN) {

        // 计算权重系数 alpha
        double alpha = CollaborativeFilteringAlgorithm.calculateHybridWeight(behaviorCount);

        // 获取已观看视频ID集合
        Set<String> watchedVideoIds = userActions.stream()
                .map(UserAction::getVideoId)
                .collect(Collectors.toSet());

        // 1. 协同过滤推荐
        List<String> cfVideoIds = performCollaborativeFiltering(userId, watchedVideoIds, topN * 2);

        // 2. 内容推荐
        List<String> contentVideoIds = performContentBasedRecommendation(userId, userActions, watchedVideoIds, topN * 2);

        // 3. 加权融合
        Map<String, Double> fusedScores = fuseRecommendations(cfVideoIds, contentVideoIds, alpha);

        // 4. 获取视频详情并排序
        List<VideoInfo> recommendList = getVideoDetailsByFusedScores(fusedScores, topN);

        return RecommendationResult.builder()
                .recommendList(recommendList)
                .recommendationType(RecommendationTypeEnum.HYBRID.getDesc())
                .confidence(alpha)
                .generateTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 执行协同过滤推荐
     */
    private List<String> performCollaborativeFiltering(String userId, Set<String> watchedVideoIds, int candidateSize) {
        try {
            // 构建用户-视频评分矩阵
            Map<String, Map<String, Double>> userVideoRatings = buildUserVideoRatingsMatrix();

            // 计算用户相似度
            Map<String, Double> userSimilarities = cfAlgorithm.calculateUserSimilarities(userId, userVideoRatings);

            if (userSimilarities.isEmpty()) {
                log.warn("无法找到相似用户: userId={}", userId);
                return Collections.emptyList();
            }

            // 生成推荐
            return cfAlgorithm.recommendByUserCF(userId, userVideoRatings, userSimilarities, watchedVideoIds, candidateSize);
        } catch (Exception e) {
            log.error("协同过滤推荐失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 执行基于内容的推荐
     */
    private List<String> performContentBasedRecommendation(
            String userId,
            List<UserAction> userActions,
            Set<String> watchedVideoIds,
            int candidateSize) {

        try {
            // 构建用户兴趣向量
            List<Map<String, Object>> behaviors = convertToBehaviorList(userActions);
            double[] userInterestVector = contentAlgorithm.buildUserInterestVector(behaviors);

            // 获取所有视频特征
            Map<String, double[]> videoFeatures = buildAllVideoFeatures();

            // 生成推荐
            List<Map.Entry<String, Double>> scoredVideos = contentAlgorithm.recommendByContent(
                    userInterestVector, videoFeatures, watchedVideoIds, candidateSize);

            return scoredVideos.stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("内容推荐失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 融合推荐结果
     */
    private Map<String, Double> fuseRecommendations(
            List<String> cfVideoIds,
            List<String> contentVideoIds,
            double alpha) {

        Map<String, Double> fusedScores = new HashMap<>();

        // CF得分归一化
        for (int i = 0; i < cfVideoIds.size(); i++) {
            String videoId = cfVideoIds.get(i);
            double score = alpha * (1.0 / (i + 1)); // 位置倒数作为得分
            fusedScores.merge(videoId, score, Double::sum);
        }

        // 内容推荐得分归一化
        for (int i = 0; i < contentVideoIds.size(); i++) {
            String videoId = contentVideoIds.get(i);
            double score = (1 - alpha) * (1.0 / (i + 1));
            fusedScores.merge(videoId, score, Double::sum);
        }

        return fusedScores;
    }

    /**
     * 根据融合得分获取视频详情
     */
    private List<VideoInfo> getVideoDetailsByFusedScores(Map<String, Double> fusedScores, int topN) {
        // 按得分排序
        List<String> sortedVideoIds = fusedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 批量查询视频详情
        List<VideoInfo> videos = new ArrayList<>();
        for (String videoId : sortedVideoIds) {
            VideoInfo video = videoInfoMapper.selectByVideoId(videoId);
            if (video != null) {
                videos.add(video);
            }
        }

        return videos;
    }

    /**
     * 获取用户行为列表
     */
    private List<UserAction> getUserActions(String userId) {
        UserActionQuery query = new UserActionQuery();
        query.setUserId(userId);
        // 只统计有效的互动行为（点赞、收藏、投币、评论、弹幕）
        query.setActionTypeArray(new Integer[]{
                UserActionTypeEnum.VIDEO_LIKE.getType(),
                UserActionTypeEnum.VIDEO_COLLECT.getType(),
                UserActionTypeEnum.VIDEO_COIN.getType(),
                UserActionTypeEnum.VIDEO_COMMENT.getType(),
                UserActionTypeEnum.VIDEO_DANMU.getType()
        });
        return userActionMapper.selectList(query);
    }

    /**
     * 构建用户-视频评分矩阵
     */
    private Map<String, Map<String, Double>> buildUserVideoRatingsMatrix() {
        // 批量查询所有用户的视频行为
        List<UserAction> recentActions = userActionMapper.selectAllUserVideoActions(1000);

        Map<String, Map<String, Double>> ratings = new HashMap<>();
        for (UserAction action : recentActions) {
            ratings.computeIfAbsent(action.getUserId(), k -> new HashMap<>());
            double weight = CollaborativeFilteringAlgorithm.getActionWeight(action.getActionType());
            ratings.get(action.getUserId()).merge(action.getVideoId(), weight, Double::sum);
        }

        return ratings;
    }

    /**
     * 转换为用户行为列表（用于内容推荐）
     */
    private List<Map<String, Object>> convertToBehaviorList(List<UserAction> userActions) {
        List<Map<String, Object>> behaviors = new ArrayList<>();

        for (UserAction action : userActions) {
            VideoInfo video = videoInfoMapper.selectByVideoId(action.getVideoId());
            if (video != null) {
                Map<String, Object> behavior = new HashMap<>();
                behavior.put("categoryId", video.getCategoryId());
                behavior.put("pCategoryId", video.getPCategoryId());

                Set<String> tags = new HashSet<>();
                if (video.getTags() != null && !video.getTags().isEmpty()) {
                    tags = Arrays.stream(video.getTags().split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet());
                }
                behavior.put("tags", tags);
                behavior.put("actionType", action.getActionType());

                behaviors.add(behavior);
            }
        }

        return behaviors;
    }

    /**
     * 构建所有视频特征
     */
    private Map<String, double[]> buildAllVideoFeatures() {
        Map<String, double[]> features = new HashMap<>();

        // 查询所有视频（实际应该分页或限制数量）
        VideoInfoQuery query = new VideoInfoQuery();
        query.setPageSize(1000);
        List<VideoInfo> videos = videoInfoMapper.selectList(query);

        for (VideoInfo video : videos) {
            Set<String> tags = new HashSet<>();
            if (video.getTags() != null && !video.getTags().isEmpty()) {
                tags = Arrays.stream(video.getTags().split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());
            }

            double[] featureVector = contentAlgorithm.buildVideoFeatureVector(
                    video.getCategoryId(),
                    video.getPCategoryId(),
                    tags,
                    video.getVideoName()
            );

            features.put(video.getVideoId(), featureVector);
        }

        return features;
    }

    /**
     * 获取全分类均衡视频
     */
    private List<VideoInfo> getCategoryBalancedVideos(int count) {
        // 简单实现：随机选择不同分类的视频
        VideoInfoQuery query = new VideoInfoQuery();
        query.setOrderBy("create_time desc");
        query.setPageSize(count);
        return videoInfoMapper.selectList(query);
    }
}
