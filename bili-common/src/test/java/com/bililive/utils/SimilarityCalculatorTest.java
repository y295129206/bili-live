package com.bililive.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimilarityCalculator 单元测试（教学示例 · 第一批）
 *
 * 被测对象是纯静态数学函数，无外部依赖 —— 这是单元测试最理想的起点：
 * 输入确定、输出确定、无需 Mock、无需 Spring 容器。
 *
 * 覆盖的测试设计方法：
 *  - 等价类划分：正常向量 / 零向量 / null / 长度不等
 *  - 边界值分析：空数组、单元素、全 0、全相同
 *  - 参数化测试：一组输入输出数据表驱动多个用例
 */
@DisplayName("相似度计算工具 单元测试")
class SimilarityCalculatorTest {

    private static final double DELTA = 1e-9;

    // ========== 余弦相似度 ==========

    @Test
    @DisplayName("余弦相似度：完全相同的向量应为 1")
    void cosineIdenticalVectorsShouldBeOne() {
        double[] a = {1.0, 2.0, 3.0, 4.0};
        double[] b = {1.0, 2.0, 3.0, 4.0};

        double result = SimilarityCalculator.calculateCosineSimilarity(a, b);

        assertEquals(1.0, result, DELTA);
    }

    @Test
    @DisplayName("余弦相似度：正交向量应为 0")
    void cosineOrthogonalVectorsShouldBeZero() {
        double[] a = {1.0, 0.0};
        double[] b = {0.0, 1.0};

        double result = SimilarityCalculator.calculateCosineSimilarity(a, b);

        assertEquals(0.0, result, DELTA);
    }

    @Test
    @DisplayName("余弦相似度：方向相反的向量应为 -1")
    void cosineOppositeVectorsShouldBeMinusOne() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {-1.0, -2.0, -3.0};

        double result = SimilarityCalculator.calculateCosineSimilarity(a, b);

        assertEquals(-1.0, result, DELTA);
    }

    @Test
    @DisplayName("余弦相似度：已知数学结果验证（勾股向量）")
    void cosineKnownValue() {
        // a·b = 1*1 + 2*0 + 3*0 = 1; |a|=sqrt(14), |b|=1 => 1/sqrt(14)
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {1.0, 0.0, 0.0};

        double result = SimilarityCalculator.calculateCosineSimilarity(a, b);

        assertEquals(1.0 / Math.sqrt(14), result, DELTA);
    }

    @ParameterizedTest(name = "a={0}, b={1} -> 期望 {2}")
    @CsvSource({
            "1.0, 1.0, 1.0",
            "1.0, -1.0, -1.0",
            "0.0, 1.0, 0.0",
            "2.0, 3.0, 1.0"
    })
    @DisplayName("余弦相似度：一维向量的参数化测试")
    void cosineSingleDimension(double a, double b, double expected) {
        assertEquals(expected,
                SimilarityCalculator.calculateCosineSimilarity(new double[]{a}, new double[]{b}),
                DELTA);
    }

    @Test
    @DisplayName("余弦相似度：零向量（除零保护）应返回 0")
    void cosineZeroVectorShouldReturnZero() {
        double[] zero = {0.0, 0.0, 0.0};
        double[] normal = {1.0, 2.0, 3.0};

        assertAll(
                () -> assertEquals(0.0, SimilarityCalculator.calculateCosineSimilarity(zero, normal), DELTA),
                () -> assertEquals(0.0, SimilarityCalculator.calculateCosineSimilarity(normal, zero), DELTA),
                () -> assertEquals(0.0, SimilarityCalculator.calculateCosineSimilarity(zero, zero), DELTA)
        );
    }

    @Test
    @DisplayName("余弦相似度：null 参数应返回 0 而不是抛异常")
    void cosineNullInputShouldReturnZero() {
        double[] normal = {1.0, 2.0};

        assertAll(
                () -> assertEquals(0.0, SimilarityCalculator.calculateCosineSimilarity(null, normal), DELTA),
                () -> assertEquals(0.0, SimilarityCalculator.calculateCosineSimilarity(normal, null), DELTA),
                () -> assertEquals(0.0, SimilarityCalculator.calculateCosineSimilarity(null, null), DELTA)
        );
    }

    @Test
    @DisplayName("余弦相似度：长度不一致应返回 0")
    void cosineDifferentLengthShouldReturnZero() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0, 2.0, 3.0};

        assertEquals(0.0, SimilarityCalculator.calculateCosineSimilarity(a, b), DELTA);
    }

    @Test
    @DisplayName("余弦相似度：空数组应返回 0")
    void cosineEmptyArrayShouldReturnZero() {
        assertEquals(0.0,
                SimilarityCalculator.calculateCosineSimilarity(new double[0], new double[0]), DELTA);
    }

    // ========== 皮尔逊相关系数 ==========

    @Test
    @DisplayName("皮尔逊：完全正相关应为 1")
    void pearsonPerfectPositive() {
        double[] a = {1.0, 2.0, 3.0, 4.0};
        double[] b = {2.0, 4.0, 6.0, 8.0}; // b = 2a，线性正相关

        assertEquals(1.0, SimilarityCalculator.calculatePearsonCorrelation(a, b), DELTA);
    }

    @Test
    @DisplayName("皮尔逊：完全负相关应为 -1")
    void pearsonPerfectNegative() {
        double[] a = {1.0, 2.0, 3.0, 4.0};
        double[] b = {8.0, 6.0, 4.0, 2.0}; // b 随 a 增大而减小

        assertEquals(-1.0, SimilarityCalculator.calculatePearsonCorrelation(a, b), DELTA);
    }

    @Test
    @DisplayName("皮尔逊：常量向量（方差为 0）应返回 0，避免除零")
    void pearsonConstantVectorShouldReturnZero() {
        double[] constant = {5.0, 5.0, 5.0};
        double[] normal = {1.0, 2.0, 3.0};

        assertAll(
                () -> assertEquals(0.0, SimilarityCalculator.calculatePearsonCorrelation(constant, normal), DELTA),
                () -> assertEquals(0.0, SimilarityCalculator.calculatePearsonCorrelation(normal, constant), DELTA)
        );
    }

    // ========== 权重归一化 ==========

    @Test
    @DisplayName("归一化：权重之和应为 1")
    void normalizeWeightsSumTo1() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("a", 1.0);
        weights.put("b", 3.0);
        weights.put("c", 6.0);

        Map<String, Double> result = SimilarityCalculator.normalizeWeights(weights);

        assertEquals(0.1, result.get("a"), DELTA);
        assertEquals(0.3, result.get("b"), DELTA);
        assertEquals(0.6, result.get("c"), DELTA);
        assertEquals(1.0, result.values().stream().mapToDouble(Double::doubleValue).sum(), DELTA);
    }

    @Test
    @DisplayName("归一化：空 Map 应返回空 Map")
    void normalizeEmptyMap() {
        assertTrue(SimilarityCalculator.normalizeWeights(new HashMap<>()).isEmpty());
        assertTrue(SimilarityCalculator.normalizeWeights(null).isEmpty());
    }

    @Test
    @DisplayName("归一化：全 0 权重应原样返回（避免除零）")
    void normalizeAllZero() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("a", 0.0);
        weights.put("b", 0.0);

        Map<String, Double> result = SimilarityCalculator.normalizeWeights(weights);

        assertEquals(0.0, result.get("a"), DELTA);
        assertEquals(0.0, result.get("b"), DELTA);
    }
}
