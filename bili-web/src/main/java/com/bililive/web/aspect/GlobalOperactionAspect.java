package com.bililive.web.aspect;

import com.bililive.annotation.GlobalInterceptor;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class GlobalOperactionAspect {

    @Autowired
    private RedisUtils redisUtils;

    public GlobalOperactionAspect(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @Before("@annotation(com.bililive.annotation.GlobalInterceptor)")
    public void before(JoinPoint joinPoint) {
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        GlobalInterceptor globalInterceptor = method.getAnnotation(GlobalInterceptor.class);
        if (globalInterceptor== null) {
            return;
        }

        if(globalInterceptor.checkLogin()){
            checkLogin();
        }
    }

    private void checkLogin() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        if(token == null){
            throw new BaseException(901,"请登录");
        }
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto)redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB+token);
        if(tokenUserInfoDto == null){
            throw new BaseException(901,"请登录");
        }
    }
}
