package com.bililive.mapper;

import com.bililive.pojo.UserMessage;
import com.bililive.pojo.UserMessageCountDto;
import com.bililive.pojo.UserMessageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户消息 Mapper 接口
 */
@Mapper
public interface UserMessageMapper {

    /**
     * 插入消息记录
     * @param userMessage 消息对象
     */
    void insert(UserMessage userMessage);

    /**
     * 根据消息ID查询消息
     * @param messageId 消息ID
     * @return 消息记录
     */
    UserMessage selectByMessageId(@Param("messageId") Integer messageId);

    /**
     * 根据消息ID删除消息
     * @param messageId 消息ID
     * @param userId 用户ID（防止误删）
     */
    void deleteByMessageId(@Param("messageId") Integer messageId, @Param("userId") String userId);

    /**
     * 批量删除消息
     * @param messageIdList 消息ID列表
     * @param userId 用户ID
     */
    void deleteBatchByMessageId(@Param("messageIdList") List<Integer> messageIdList, @Param("userId") String userId);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void updateReadType(@Param("messageId") Integer messageId, @Param("userId") String userId);

    /**
     * 批量标记消息为已读
     * @param userId 用户ID
     * @param messageType 消息类型（可为null，表示全部类型）
     */
    void updateReadTypeBatch(@Param("userId") String userId, @Param("messageType") Integer messageType);

    /**
     * 根据查询条件获取消息列表
     * @param query 查询参数
     * @return 消息列表
     */
    List<UserMessage> selectList(@Param("query") UserMessageQuery query);

    /**
     * 根据查询条件统计消息总数
     * @param query 查询参数
     * @return 总数
     */
    Integer selectCount(@Param("query") UserMessageQuery query);


    List<UserMessageCountDto> getMessageTypeNoReadCount(@Param("userId") String userId);

    /**
     * 根据查询条件更新消息
     * @param userMessage 要更新的消息对象
     * @param query 查询条件
     */
    void updateByParam(@Param("userMessage") UserMessage userMessage, @Param("query") UserMessageQuery query);

    /**
     * 根据查询条件删除消息
     * @param query 查询条件
     */
    void deleteByParam(@Param("query") UserMessageQuery query);
}
