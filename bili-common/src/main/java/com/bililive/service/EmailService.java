package com.bililive.service;

/**
 * 邮箱验证码服务（注册功能）
 */
public interface EmailService {

    /**
     * 发送邮箱验证码
     * 校验邮箱格式、是否已注册、60 秒限流，然后生成 6 位验证码并通过 QQ SMTP 发送。
     * @param email 注册邮箱（仅支持 QQ 邮箱）
     */
    void sendEmailCode(String email);

    /**
     * 校验邮箱验证码是否正确。
     * 校验通过后立即删除（一次性）；连续输错达到上限后验证码作废，需重新发送。
     * @param email 注册邮箱
     * @param code  用户输入的 6 位验证码
     * @throws com.bililive.pojo.BaseException 验证码错误/过期/超限
     */
    void verifyEmailCode(String email, String code);
}
