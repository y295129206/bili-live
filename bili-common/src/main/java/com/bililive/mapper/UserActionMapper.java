package com.bililive.mapper;

import com.bililive.pojo.UserAction;
import com.bililive.pojo.UserActionQuery;
import com.bililive.pojo.VideoInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户行为 Mapper 接口
 */
@Mapper
public interface UserActionMapper {

    /**
     * 插入用户行为记录
     * @param userAction 用户行为对象
     */
    void insert(UserAction userAction);

    /**
     * 根据用户 ID、视频 ID 和行为类型查询用户行为
     * @param userId 用户 ID
     * @param videoId 视频 ID
     * @param actionType 行为类型
     * @return 用户行为记录
     */
    UserAction selectByUserIdAndVideoIdAndActionType(
            @Param("userId") String userId,
            @Param("videoId") String videoId,
            @Param("actionType") Integer actionType);

    /**
     * 根据用户 ID、视频 ID、行为类型和评论 ID 查询用户行为
     * @param userId 用户 ID
     * @param videoId 视频 ID
     * @param actionType 行为类型
     * @param commentId 评论 ID
     * @return 用户行为记录
     */
    UserAction selectByUserIdAndVideoIdAndActionTypeAndCommentId(
            @Param("userId") String userId,
            @Param("videoId") String videoId,
            @Param("actionType") Integer actionType,
            @Param("commentId") Integer commentId);

    /**
     * 根据行为 ID 更新用户行为记录
     * @param actionId 行为 ID
     */
    void updateByActionId(@Param("actionId") Integer actionId);

    /**
     * 根据行为 ID 删除用户行为记录
     * @param actionId 行为 ID
     */
    void deleteByActionId(@Param("actionId") Integer actionId);

    /**
     * 根据查询条件获取用户行为列表
     * @param query 查询参数
     * @return 用户行为列表
     */
    List<UserAction> selectList(@Param("query") UserActionQuery query);

    /**
     * 根据查询条件统计用户行为总数
     * @param query 查询参数
     * @return 总数
     */
    Integer selectCount(@Param("query") UserActionQuery query);
}
