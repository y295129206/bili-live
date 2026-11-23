package com.bililive.web.controller;

import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.RecommendationResult;
import com.bililive.pojo.Result;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.service.VideoRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;

/**
 * 视频推荐控制器
 */
@RestController
@RequestMapping("/recommendation")
@Validated
@Slf4j
public class VideoRecommendationController {

    @Autowired
    private VideoRecommendationService recommendationService;

    @Autowired
    private RedisComponent redisComponent;

    /**
     * 获取个性化推荐列表
     * @param topN 推荐数量（默认10）
     * @param request HTTP请求
     * @return 推荐结果
     */
    @RequestMapping("/getPersonalizedRecommendation")
    @GlobalInterceptor(checkLogin = true)
    public Result<RecommendationResult> getPersonalizedRecommendation(
            @Min(1) Integer topN,
            HttpServletRequest request) {

        if (topN == null || topN < 1) {
            topN = 10;
        }

        String userId = getTokenUserInfoDto(request).getUserId();
        RecommendationResult result = recommendationService.getPersonalizedRecommendation(userId, topN);
        log.info("获取个性化推荐列表，用户ID：{}，推荐数量：{}，结果：{}", userId, topN, result);

        return Result.success(result);
    }

    /**
     * 获取热门推荐列表
     * @param topN 推荐数量（默认10）
     * @return 推荐结果
     */
    @RequestMapping("/getHotRecommendation")
    public Result<RecommendationResult> getHotRecommendation(@Min(1) Integer topN) {
        if (topN == null || topN < 1) {
            topN = 10;
        }

        RecommendationResult result = recommendationService.getHotRecommendation(topN);
        return Result.success(result);
    }

    /**
     * 获取冷启动推荐列表
     * @param topN 推荐数量（默认10）
     * @param request HTTP请求
     * @return 推荐结果
     */
    @RequestMapping("/getColdStartRecommendation")
    @GlobalInterceptor(checkLogin = true)
    public Result<RecommendationResult> getColdStartRecommendation(
            @Min(1) Integer topN,
            HttpServletRequest request) {

        if (topN == null || topN < 1) {
            topN = 10;
        }

        String userId = getTokenUserInfoDto(request).getUserId();
        RecommendationResult result = recommendationService.getColdStartRecommendation(userId, topN);

        return Result.success(result);
    }

    /**
     * 清除推荐缓存
     * @param request HTTP请求
     * @return 操作结果
     */
    @RequestMapping("/clearCache")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> clearCache(HttpServletRequest request) {
        String userId = getTokenUserInfoDto(request).getUserId();
        recommendationService.clearRecommendationCache(userId);
        return Result.success("缓存已清除");
    }

    /**
     * 获取Token用户信息
     */
    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request) {
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
