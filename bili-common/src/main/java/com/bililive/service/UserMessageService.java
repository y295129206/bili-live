package com.bililive.service;

import com.bililive.Enum.MessageTypeEnum;
import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.UserMessage;
import com.bililive.pojo.UserMessageCountDto;
import com.bililive.pojo.UserMessageQuery;

import java.util.List;

/**
 * 用户消息服务接口
 */
public interface UserMessageService {

    /**
     * 保存用户消息
     * @param videoId 视频ID
     * @param sendUserId 发送人ID
     * @param messageType 消息类型
     * @param content 消息内容
     * @param replyCommendId 回复的评论ID（可选）
     */
    void saveUserMessage(String videoId, String sendUserId, MessageTypeEnum messageType, String content, Integer replyCommendId);

    /**
     * 根据查询条件统计消息数量
     * @param query 查询参数
     * @return 消息数量
     */
    Integer findCountByParam(UserMessageQuery query);

    /**
     * 获取各消息类型的未读数量
     * @param userId 用户ID
     * @return 消息类型统计列表
     */
    List<UserMessageCountDto> getMessageTypeNoReadCount(String userId);

    /**
     * 根据查询条件更新消息
     * @param userMessage 要更新的消息对象
     * @param query 查询条件
     */
    void updateByParam(UserMessage userMessage, UserMessageQuery query);

    /**
     * 分页查询消息列表
     * @param query 查询参数
     * @return 分页结果
     */
    PaginationResult<UserMessage> findListByPage(UserMessageQuery query);

    /**
     * 根据查询条件删除消息
     * @param query 查询条件
     */
    void deleteByParam(UserMessageQuery query);
}
