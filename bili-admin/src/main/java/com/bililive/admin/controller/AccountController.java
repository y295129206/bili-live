package com.bililive.admin.controller;

import com.bililive.component.RedisComponent;
import com.bililive.entity.config.AppConfig;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.Result;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.redis.RedisUtils;
import com.bililive.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private AppConfig appConfig;

    @RequestMapping("/checkCode")
    public Result<Map<String,String>> checkCode(){
        //生成验证图片
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100,42);
        String code = captcha.text();
        log.info("code:{}",code);
        String checkCodeKey = redisComponent.saveCheckCode(code);
        String checkCodeBase64 = captcha.toBase64();
        Map<String,String> result = new HashMap<>();
        result.put("checkCodeKey",checkCodeKey);
        result.put("checkCode",checkCodeBase64);
        log.info("result:{}",result);
        return Result.success(result);

    }
    @RequestMapping("/login")
    public Result<String> login(HttpServletRequest request,
                        HttpServletResponse response,
                        @NotEmpty String account,
                        @NotEmpty String password,
                        @NotEmpty String checkCodeKey,
                        @NotEmpty String checkCode){
        try{
            if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
                throw new BaseException("验证码出错");
            }
            if(!account.equals(appConfig.getAdminAccount())||!password.equals(DigestUtils.md5DigestAsHex(appConfig.getAdminPassword().getBytes()).toUpperCase())){
                throw new BaseException("账号或密码错误");
            }
            String token =redisComponent.saveTokenforAdmin(account);
            saveToken2Cookie(response,token);
            return Result.success(token);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
            Cookie[] cookies = request.getCookies();
            String token = null;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(Constants.TOKEN_ADMIN)) {
                    token = cookie.getValue();
                }
            }
            if (token != null) {
                redisComponent.cleanTokenforAdmin(token);
            }
        }
    }

    protected String getIpAddr() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    protected void saveToken2Cookie(HttpServletResponse response,String token){
        Cookie cookie = new Cookie(Constants.TOKEN_ADMIN,token);
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    protected void cleanCookie(HttpServletResponse response){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return;
        }
        for(Cookie cookie : cookies){
            if(cookie.getName().equals(Constants.TOKEN_ADMIN)){
                redisComponent.cleanTokenforAdmin(cookie.getValue());
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
                break;
            }
        }
    }
    @RequestMapping("/logout")
    public Result logout(HttpServletResponse response){
        cleanCookie(response);
        return Result.success();
    }
}
