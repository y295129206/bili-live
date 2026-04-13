package com.bililive.web.controller;

import com.bililive.Enum.MessageReadTypeEnum;
import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.service.UserMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/message")
@Slf4j
public class UserMessageController {
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/getNoReadCount")
    @GlobalInterceptor(checkLogin = true)
    public Result<Integer> getNoReadCount(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserMessageQuery query = new UserMessageQuery();
        query.setUserId(tokenUserInfoDto.getUserId());
        query.setReadType(MessageReadTypeEnum.NO_READ.getType());
        Integer count = userMessageService.findCountByParam(query);
        return Result.success(count);
    }
    
    @RequestMapping("/getNoReadCountGroup")
    @GlobalInterceptor(checkLogin = true)
    public Result<List<UserMessageCountDto>> getNoReadCountGroup(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        List<UserMessageCountDto> dataList = userMessageService.getMessageTypeNoReadCount(tokenUserInfoDto.getUserId());
        return Result.success(dataList);
    }

    @RequestMapping("/readAll")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> readAll(Integer messageType,HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserMessageQuery query = new UserMessageQuery();
        query.setUserId(tokenUserInfoDto.getUserId());
        query.setMessageType(messageType);
        UserMessage userMessage = new UserMessage();
        userMessage.setReadType(MessageReadTypeEnum.READ.getType());
        userMessageService.updateByParam(userMessage, query);
        return Result.success();
    }

    @RequestMapping("/loadMessage")
    @GlobalInterceptor(checkLogin = true)
    public Result<PaginationResult<UserMessage>> loadMessage(@NotNull Integer messageType,Integer pageNo, HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserMessageQuery query = new UserMessageQuery();
        query.setUserId(tokenUserInfoDto.getUserId());
        query.setPageNo(pageNo);
        query.setOrderBy("create_time desc");
        query.setMessageType(messageType);
        PaginationResult<UserMessage> dataList = userMessageService.findListByPage(query);
        return Result.success(dataList);
    }

    @RequestMapping("/delMessage")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> delMessage(Integer messageId,HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserMessageQuery query = new UserMessageQuery();
        query.setUserId(tokenUserInfoDto.getUserId());
        query.setMessageId(messageId);
        userMessageService.deleteByParam(query);
        return Result.success();
    }

    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
