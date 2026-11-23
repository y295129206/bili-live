package com.bililive.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 相似度计算工具类
 */
@Slf4j
public class SimilarityCalculator {

    /**
     * 计算余弦相似度
     * @param vectorA 向量A
     * @param vectorB 向量B
     * @return 余弦相似度值 [-1, 1]
     */
    public static double calculateCosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 计算皮尔逊相关系数
     * @param vectorA 向量A
     * @param vectorB 向量B
     * @return 皮尔逊相关系数 [-1, 1]
     */
    public static double calculatePearsonCorrelation(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            return 0.0;
        }

        int n = vectorA.length;
        if (n == 0) {
            return 0.0;
        }

        // 计算均值
        double sumA = 0.0;
        double sumB = 0.0;
        for (int i = 0; i < n; i++) {
            sumA += vectorA[i];
            sumB += vectorB[i];
        }
        double meanA = sumA / n;
        double meanB = sumB / n;

        // 计算皮尔逊相关系数
        double numerator = 0.0;
        double denomA = 0.0;
        double denomB = 0.0;

        for (int i = 0; i < n; i++) {
            double diffA = vectorA[i] - meanA;
            double diffB = vectorB[i] - meanB;
            numerator += diffA * diffB;
            denomA += diffA * diffA;
            denomB += diffB * diffB;
        }

        if (denomA == 0 || denomB == 0) {
            return 0.0;
        }

        return numerator / (Math.sqrt(denomA) * Math.sqrt(denomB));
    }

    /**
     * 归一化权重
     * @param weights 原始权重
     * @return 归一化后的权重
     */
    public static java.util.Map<String, Double> normalizeWeights(java.util.Map<String, Double> weights) {
        if (weights == null || weights.isEmpty()) {
            return new java.util.HashMap<>();
        }

        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum == 0) {
            return weights;
        }

        java.util.Map<String, Double> normalized = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, Double> entry : weights.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() / sum);
        }

        return normalized;
    }
}
