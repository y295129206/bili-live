package com.bililive.admin.interceptor;

import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.BaseException;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Component
@Slf4j
public class AppInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisComponent redisComponent;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(null==handler){
            return false ;
        }
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        if(request.getRequestURI().contains("/account")){
            return true;
        }
        String token = request.getHeader(Constants.TOKEN_ADMIN);
        if(token==null){
            token = getTokenFormCookie(request);
        }
        log.info("token:{}",token);
        if(token==null){
            throw new BaseException(901,"未登录或登录超时");
        }
        Object sessionObj = redisComponent.getTokenInfoforAdmin(token);
        log.info("sessionObj:{}",sessionObj);
        if(sessionObj==null){
            throw new BaseException(901,"未登录或登录超时");
        }
        return true;
    }
    private String getTokenFormCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return null;
        }
        String token = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Constants.TOKEN_ADMIN)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
