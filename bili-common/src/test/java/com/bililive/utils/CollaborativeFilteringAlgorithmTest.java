package com.bililive.utils;

import com.bililive.Enum.UserActionTypeEnum;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CollaborativeFilteringAlgorithm 单元测试（教学示例 · 第二批）
 *
 * 重点：
 *  1. 实例方法（无状态）如何直接 new 出来测试 —— 不需要 Spring 容器
 *  2. 用测试"锁定"行为权重等业务规则
 *  3. 【核心】用测试暴露"冷启动阈值"缺陷 —— 测试即规格（Testing as Specification）
 */
@DisplayName("协同过滤算法 单元测试")
class CollaborativeFilteringAlgorithmTest {

    private final CollaborativeFilteringAlgorithm algorithm = new CollaborativeFilteringAlgorithm();

    // ========== 行为权重：把业务规则固化成测试 ==========

    @Test
    @DisplayName("行为权重：投币(5) > 收藏(4) > 点赞(3) > 评论/弹幕(2) > 播放(1)")
    void actionWeightsShouldMatchDesign() {
        assertAll(
                () -> assertEquals(5.0, CollaborativeFilteringAlgorithm.getActionWeight(UserActionTypeEnum.VIDEO_COIN.getType())),
                () -> assertEquals(4.0, CollaborativeFilteringAlgorithm.getActionWeight(UserActionTypeEnum.VIDEO_COLLECT.getType())),
                () -> assertEquals(3.0, CollaborativeFilteringAlgorithm.getActionWeight(UserActionTypeEnum.VIDEO_LIKE.getType())),
                () -> assertEquals(2.0, CollaborativeFilteringAlgorithm.getActionWeight(UserActionTypeEnum.VIDEO_COMMENT.getType())),
                () -> assertEquals(2.0, CollaborativeFilteringAlgorithm.getActionWeight(UserActionTypeEnum.VIDEO_DANMU.getType())),
                () -> assertEquals(1.0, CollaborativeFilteringAlgorithm.getActionWeight(UserActionTypeEnum.VIDEO_PLAY.getType()))
        );
    }

    @Test
    @DisplayName("行为权重：未知类型应降级为默认权重 1.0")
    void unknownActionWeightShouldFallbackToOne() {
        assertEquals(1.0, CollaborativeFilteringAlgorithm.getActionWeight(999));
    }

    // ========== 用户相似度（基于共同评分视频的余弦相似度）==========

    @Test
    @DisplayName("用户相似度：目标用户不存在时应返回空 Map")
    void similarityTargetUserMissing() {
        Map<String, Map<String, Double>> ratings = new HashMap<>();
        ratings.put("u2", Map.of("v1", 5.0));

        assertTrue(algorithm.calculateUserSimilarities("u1", ratings).isEmpty());
    }

    @Test
    @DisplayName("用户相似度：无共同视频的用户不产生相似度")
    void similarityNoCommonVideos() {
        Map<String, Map<String, Double>> ratings = new HashMap<>();
        ratings.put("u1", Map.of("v1", 5.0, "v2", 3.0));
        ratings.put("u2", Map.of("v3", 4.0, "v4", 2.0));

        assertTrue(algorithm.calculateUserSimilarities("u1", ratings).isEmpty());
    }

    @Test
    @DisplayName("用户相似度：评分完全一致的两用户相似度应为 1")
    void similarityIdenticalRatingsShouldBeOne() {
        Map<String, Map<String, Double>> ratings = new HashMap<>();
        ratings.put("u1", Map.of("v1", 5.0, "v2", 3.0, "v3", 4.0));
        ratings.put("u2", Map.of("v1", 5.0, "v2", 3.0, "v3", 4.0));

        Map<String, Double> result = algorithm.calculateUserSimilarities("u1", ratings);

        assertEquals(1, result.size());
        assertEquals(1.0, result.get("u2"), 1e-9);
    }

    @Test
    @DisplayName("用户相似度：结果不应包含目标用户自己")
    void similarityShouldExcludeTargetUser() {
        Map<String, Map<String, Double>> ratings = new HashMap<>();
        ratings.put("u1", Map.of("v1", 5.0));

        assertFalse(algorithm.calculateUserSimilarities("u1", ratings).containsKey("u1"));
    }

    // ========== 基于用户的协同过滤推荐 ==========

    @Test
    @DisplayName("UserCF：邻居喜欢但目标未看过的视频应被推荐")
    void userCFShouldRecommendUnwatchedVideos() {
        Map<String, Map<String, Double>> ratings = new HashMap<>();
        ratings.put("u1", Map.of("v1", 5.0));
        ratings.put("u2", Map.of("v1", 5.0, "v2", 4.0, "v3", 3.0));
        Map<String, Double> similarities = Map.of("u2", 0.9);
        Set<String> watched = Set.of("v1");

        List<String> result = algorithm.recommendByUserCF("u1", ratings, similarities, watched, 2);

        assertEquals(2, result.size());
        assertTrue(result.contains("v2")); // 得分 0.9*4=3.6，高于 v3 的 0.9*3=2.7
        assertTrue(result.contains("v3"));
        assertEquals("v2", result.get(0)); // 验证排序：高分在前
        assertFalse(result.contains("v1")); // 已看过的被排除
    }

    // ========== ItemCF / Jaccard ==========

    @Test
    @DisplayName("ItemCF 物品相似度：Jaccard 交集/并集计算正确")
    void itemSimilarityJaccard() {
        Map<String, Set<String>> videoUsers = new HashMap<>();
        videoUsers.put("v1", Set.of("u1", "u2", "u3"));
        // 交集{u1,u2}=2；并集{u1,u2,u3,u4}=4（注意：3+3-2=4，不是5）
        videoUsers.put("v2", Set.of("u1", "u2", "u4")); // Jaccard = 2/4 = 0.5
        videoUsers.put("v3", Set.of("u5"));             // 无交集

        Map<String, Double> result = algorithm.calculateItemSimilarities("v1", videoUsers);

        assertEquals(1, result.size());
        assertEquals(0.5, result.get("v2"), 1e-9);
        assertFalse(result.containsKey("v3"));
    }

    // ========== 【教学核心】缺陷暴露测试 ==========

    /**
     * 【缺陷锁定测试】
     * 用测试固定"代码当前真实行为"：阈值=5，所以 6 条行为就算老用户。
     * 这个测试是【通过】的，它的价值是：一旦有人把阈值改成文档说的 50，
     * 这个测试会立刻失败并提醒"行为发生了变化，需同步更新相关测试与文档"。
     */
    @Test
    @DisplayName("【当前行为】阈值为5：6条行为即被视为老用户")
    void coldStartThresholdCurrentBehavior() {
        assertFalse(CollaborativeFilteringAlgorithm.isNewUser(6),
                "当前实现阈值=5，6>=5 所以是老用户");
        assertTrue(CollaborativeFilteringAlgorithm.isNewUser(4),
                "当前实现阈值=5，4<5 所以是新用户");
    }

    /**
     * 【缺陷证据 · 预期失败，故用 @Disabled 跳过】
     *  - RECOMMENDATION_SYSTEM.md 文档反复声明：行为数 >= 50 才算老用户
     *  - 但实际代码 CollaborativeFilteringAlgorithm.BEHAVIOR_THRESHOLD = 5
     *
     * 这个测试按【文档规格】编写：期望 30 条行为仍是"新用户"。
     * 若移除 @Disabled 运行，它会失败 —— 失败信息就是缺陷的可执行证据。
     *
     * 工程实践：确认规格后（到底阈值是5还是50），二选一：
     *   方案A 认为文档对 → 把代码阈值改为50，移除本测试的 @Disabled，同时改掉上面的锁定测试；
     *   方案B 认为代码对 → 修改文档为5，删除本测试。
     * 无论哪种，都必须让"文档、代码、测试"三者一致。
     */
    @Test
    @Disabled("缺陷证据用例：文档承诺阈值50，代码实际为5；确认规格前跳过以保持构建通过")
    @DisplayName("【缺陷证据】按文档规格：30条行为应仍属新用户")
    void coldStartThresholdShouldFollowDocumentation() {
        assertTrue(CollaborativeFilteringAlgorithm.isNewUser(30),
                "文档规定行为数<50即为新用户，但 isNewUser(30)=false，真实阈值是5而非50");
    }

    @Test
    @DisplayName("混合权重：行为数增加时 alpha 应单调递增")
    void hybridWeightShouldIncreaseWithBehaviorCount() {
        double w10 = CollaborativeFilteringAlgorithm.calculateHybridWeight(10);
        double w50 = CollaborativeFilteringAlgorithm.calculateHybridWeight(50);
        double w100 = CollaborativeFilteringAlgorithm.calculateHybridWeight(100);

        assertTrue(w10 < w50 && w50 < w100);
        assertTrue(w50 < 1.0 && w100 < 1.0); // 永远小于 1
    }
}
