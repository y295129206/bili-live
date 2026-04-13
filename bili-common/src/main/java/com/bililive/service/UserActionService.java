package com.bililive.service;

import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.UserAction;
import com.bililive.pojo.UserActionQuery;
import com.bililive.pojo.VideoInfo;

import java.util.List;

/**
 * 用户行为服务接口
 */
public interface UserActionService {

    /**
     * 保存用户行为记录
     * @param userAction 用户行为对象
     */
    void saveAction(UserAction userAction);

    /**
     * 根据用户 ID、视频 ID 和行为类型查询用户行为
     * @param userId 用户 ID
     * @param videoId 视频 ID
     * @param actionType 行为类型
     * @return 用户行为记录
     */
    UserAction getUserActionByParam(String userId, String videoId, Integer actionType);

    /**
     * 根据用户 ID、视频 ID、行为类型和评论 ID 查询用户行为
     * @param userId 用户 ID
     * @param videoId 视频 ID
     * @param actionType 行为类型
     * @param commentId 评论 ID
     * @return 用户行为记录
     */
    UserAction getUserActionByParamWithComment(String userId, String videoId, Integer actionType, Integer commentId);

    /**
     * 根据查询条件获取用户行为列表
     * @param query 查询参数
     * @return 用户行为列表
     */
    List<UserAction> findListByParam(UserActionQuery query);

    /**
     * 分页查询用户行为列表
     * @param query 查询参数
     * @return 分页结果
     */
    PaginationResult<UserAction> findListByPage(UserActionQuery query);
}
