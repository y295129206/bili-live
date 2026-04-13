package com.bililive.service.impl;

import com.bililive.Enum.MessageTypeEnum;
import com.bililive.mapper.UserMessageMapper;
import com.bililive.mapper.VideoCommentMapper;
import com.bililive.mapper.VideoInfoMapper;
import com.bililive.mapper.VideoInfoPostMapper;
import com.bililive.pojo.*;
import com.bililive.service.UserMessageService;
import com.bililive.service.VideoInfoService;
import com.bililive.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户消息服务实现类
 */
@Service
@Slf4j
public class UserMessageServiceImpl implements UserMessageService {

    @Autowired
    private UserMessageMapper userMessageMapper;
    @Autowired
    private VideoInfoMapper videoInfoMapper;
    @Autowired
    private VideoCommentMapper videoCommentMapper;
    @Autowired
    private VideoInfoPostMapper videoInfoPostMapper;


    @Override
    @Async
    public void saveUserMessage(String videoId, String sendUserId, MessageTypeEnum messageType, String content, Integer replyCommendId) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
        if (videoInfo == null) {
            return ;
        }
        UserMessageExtendDto extendDto = new UserMessageExtendDto();
        extendDto.setMessageContent(content);
        String userId = videoInfo.getUserId();
        if(ArrayUtils.contains(new Integer[]{MessageTypeEnum.LIKE.getType(), MessageTypeEnum.COLLECT.getType()},messageType.getType())){
            UserMessageQuery query = new UserMessageQuery();
            query.setUserId(userId);
            query.setVideoId(videoId);
            query.setMessageType(messageType.getType());
            Integer count = userMessageMapper.selectCount(query);
            if(count > 0){
                return ;
            }
        }
        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(userId);
        userMessage.setVideoId(videoId);
        userMessage.setMessageType(messageType.getType());
        userMessage.setReadType(0);
        userMessage.setCreateTime(new Date());
        userMessage.setSendUserId(sendUserId);

        if(replyCommendId!= null){
            log.info("{}",replyCommendId);
            VideoComment videoComment = videoCommentMapper.selectByCommentId(replyCommendId);
            log.info("{}",videoComment);
            if(videoComment != null){
                userId = videoComment.getUserId();
                extendDto.setMessageContentReply(videoComment.getContent());
            }
        }
        if(userId.equals(sendUserId)){
            return;
        }
        if(MessageTypeEnum.SYSTEM == messageType){
            VideoInfoPost videoInfoPost = videoInfoPostMapper.selectByVideoId(videoId);
            extendDto.setAuditStatus(videoInfoPost.getStatus());
        }
        userMessage.setUserId(userId);
        userMessage.setExtendJson(JsonUtils.convertObj2Json(extendDto));
        userMessageMapper.insert(userMessage);
    }

    @Override
    public Integer findCountByParam(UserMessageQuery query) {
        if (query == null) {
            query = new UserMessageQuery();
        }
        return userMessageMapper.selectCount(query);
    }

    @Override
    public List<UserMessageCountDto> getMessageTypeNoReadCount(String userId) {
        return userMessageMapper.getMessageTypeNoReadCount(userId);
    }

    @Override
    public void updateByParam(UserMessage userMessage, UserMessageQuery query) {
        if (userMessage == null) {
            throw new BaseException("消息对象不能为空");
        }
        if (query == null) {
            query = new UserMessageQuery();
        }
        if (query.getUserId() == null || query.getUserId().isEmpty()) {
            throw new BaseException("用户ID不能为空");
        }
        userMessageMapper.updateByParam(userMessage, query);
    }

    @Override
    public PaginationResult<UserMessage> findListByPage(UserMessageQuery query) {
        if (query == null) {
            query = new UserMessageQuery();
        }

        // 设置默认分页参数
        if (query.getPageNo() == null || query.getPageNo() < 1) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }

        // 创建 SimplePage 对象用于 MyBatis 分页
        SimplePage simplePage = new SimplePage(query.getPageNo(), query.getPageSize());
        query.setSimplePage(simplePage);
        
        // 查询数据列表
        List<UserMessage> list = userMessageMapper.selectList(query);
        
        // 查询总数（需要清除分页参数）
        query.setSimplePage(null);
        Integer totalCount = userMessageMapper.selectCount(query);
        
        // 重新设置分页参数
        query.setSimplePage(simplePage);
        
        // 计算总页数
        int totalPages = (totalCount + query.getPageSize() - 1) / query.getPageSize();

        PaginationResult<UserMessage> paginationResult = new PaginationResult<>();
        paginationResult.setList(list);
        paginationResult.setTotalCount(totalCount);
        paginationResult.setTotalPages(totalPages);
        paginationResult.setPageNo(query.getPageNo());
        paginationResult.setPageSize(query.getPageSize());
        
        return paginationResult;
    }

    @Override
    public void deleteByParam(UserMessageQuery query) {
        if (query == null) {
            throw new BaseException("查询条件不能为空");
        }
        if (query.getUserId() == null || query.getUserId().isEmpty()) {
            throw new BaseException("用户ID不能为空");
        }
        if (query.getMessageId() == null) {
            throw new BaseException("消息ID不能为空");
        }
        userMessageMapper.deleteByParam(query);
    }
}
