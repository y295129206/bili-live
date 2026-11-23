package com.bililive.web.controller;

import com.bililive.component.RedisComponent;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.Result;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.pojo.UserCountInfoDto;
import com.bililive.redis.RedisUtils;
import com.bililive.service.EmailService;
import com.bililive.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.apache.el.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bililive.entity.constants.Constants;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
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
    private EmailService emailService;

    /**
     * 发送邮箱验证码（注册功能）
     * 校验邮箱格式/是否已注册/60秒限流后，通过 QQ SMTP 发送 6 位验证码。
     */
    @RequestMapping("/sendEmailCode")
    public Result sendEmailCode(@NotEmpty @Email @Size(max = 150) String email) {
        emailService.sendEmailCode(email);
        return Result.success();
    }

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
    @RequestMapping("/register")
    public Result<Boolean> register(@NotEmpty @Email @Size(max = 150) String email,
                                    @NotEmpty @Size(max=20) String nickName,
                                    @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String registerPassword,
                                    @NotEmpty String checkCodeKey,
                                    @NotEmpty String checkCode,
                                    @NotEmpty String emailCode){
        // 规范化邮箱：与发码/验码时保持一致的 key
        email = email.trim().toLowerCase();
        try {
            // 1. 校验算术验证码（原有逻辑）
            if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
                throw new BaseException("验证码出错");
            }
            // 2. 校验邮箱验证码（新增：通过即删 / 失败5次作废）
            emailService.verifyEmailCode(email, emailCode);
            // 3. 执行注册
            Boolean result = userInfoService.register(email,nickName,registerPassword);
            return Result.success(result);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }
    @RequestMapping("/login")
    public Result<TokenUserInfoDto> login(HttpServletRequest request,
                                    HttpServletResponse response,
                        @NotEmpty @Email String email,
                        @NotEmpty String password,
                        @NotEmpty String checkCodeKey,
                        @NotEmpty String checkCode){
        try{
            if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
                throw new BaseException("验证码出错");
            }
            String ip = getIpAddr();
            TokenUserInfoDto tokenUserInfoDto = userInfoService.login(email,password,ip);
            saveToken2Cookie(response,tokenUserInfoDto.getToken());

            return Result.success(tokenUserInfoDto);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
            Cookie[] cookies = request.getCookies();
            if(cookies!=null){
                String token = null;
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(Constants.TOKEN_WEB)) {
                        token = cookie.getValue();
                    }
                }
                if (token != null) {
                    redisComponent.cleanToken(token);
                }
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
        Cookie cookie = new Cookie(Constants.TOKEN_WEB,token);
        cookie.setMaxAge(Constants.REDIS_KEY_EXPIRES_ONE_DAY*7/1000);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    protected TokenUserInfoDto getTokenUserInfoDto(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
    protected void cleanCookie(HttpServletResponse response){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return;
        }
        for(Cookie cookie : cookies){
            if(cookie.getName().equals(Constants.TOKEN_WEB)){
                redisComponent.cleanToken(cookie.getValue());
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
                break;
            }
        }
    }
    @RequestMapping("/autoLogin")
    public Result<TokenUserInfoDto> autoLogin(HttpServletResponse response){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if(tokenUserInfoDto==null){
            return Result.error(901,"登录过期");
        }
        if(tokenUserInfoDto.getExpireAt() - System.currentTimeMillis()<=Constants.REDIS_KEY_EXPIRES_ONE_DAY){
            redisComponent.saveToken(tokenUserInfoDto);
            saveToken2Cookie(response,tokenUserInfoDto.getToken());
        }
        return Result.success(tokenUserInfoDto);
    }
    @RequestMapping("/logout")
    public Result logout(HttpServletResponse response){
        cleanCookie(response);
        return Result.success();
    }
    @RequestMapping("/getUserCountInfo")
    public Result<UserCountInfoDto> getUserCountInfo(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if(tokenUserInfoDto==null){
            return Result.error(901,"登录过期");
        }
        UserCountInfoDto userCountInfoDto = userInfoService.getUserCountInfo(tokenUserInfoDto.getUserId());
        return Result.success(userCountInfoDto);
    }
}
