package com.bililive.web.aspect;

import com.bililive.Enum.MessageTypeEnum;
import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.Result;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.service.UserMessageService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Component
@Aspect
@Slf4j
public class MessageOperactionAspect {
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserMessageService userMessageService;

    @Around("@annotation(com.bililive.annotation.GlobalInterceptor)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        try {
            // 执行目标方法,保留原始返回值的泛型信息
            Object result = joinPoint.proceed();

            // 获取方法上的注解
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            GlobalInterceptor globalInterceptor = method.getAnnotation(GlobalInterceptor.class);
            
            if (globalInterceptor == null) {
                return result;
            }
            if(globalInterceptor.messageType() != MessageTypeEnum.NONE){
                saveMessage(globalInterceptor, joinPoint.getArgs(),method.getParameters());
            }
            // 直接返回原始结果,保持泛型信息不丢失
            return result;
        } catch (Throwable throwable) {
            throw new BaseException(500, throwable.getMessage());
        }
    }

    private void saveMessage(GlobalInterceptor globalInterceptor, Object[] args, Parameter[] parameters) {
        String videoId = null;
        Integer actionType = null;
        Integer replyCommendId = null;
        String content = null;
        for(int i=0;i<parameters.length;i++){
            if(parameters[i].getName().equals("videoId")){
                videoId = (String) args[i];
            }else if(parameters[i].getName().equals("actionType")){
                actionType = (Integer) args[i];
            }else if(parameters[i].getName().equals("replyCommentId")){
                replyCommendId = (Integer) args[i];
            }else if(parameters[i].getName().equals("content")){
                content = (String) args[i];
            }else if(parameters[i].getName().equals("reason")){
                content = (String) args[i];
            }
        }
        MessageTypeEnum messageType = globalInterceptor.messageType();
        if(UserActionTypeEnum.VIDEO_COLLECT.getType().equals(actionType)){
            messageType = MessageTypeEnum.COLLECT;
        }
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userMessageService.saveUserMessage(videoId, tokenUserInfoDto==null?null:tokenUserInfoDto.getUserId(), messageType, content,replyCommendId);
    }
    protected TokenUserInfoDto getTokenUserInfoDto(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
