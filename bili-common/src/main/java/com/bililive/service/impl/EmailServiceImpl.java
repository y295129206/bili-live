package com.bililive.service.impl;

import com.bililive.component.RedisComponent;
import com.bililive.entity.config.AppConfig;
import com.bililive.entity.constants.Constants;
import com.bililive.mapper.UserInfoMapper;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.EmailCodeDto;
import com.bililive.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.util.regex.Pattern;

/**
 * 邮箱验证码服务实现（QQ 邮箱 SMTP）
 *
 * 职责：
 *  1. sendEmailCode：校验邮箱 → 判重 → 限流 → 生成 6 位码 → 存 Redis → 发信
 *  2. verifyEmailCode：比对 → 通过即删 / 失败计数累加 / 达上限作废
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private AppConfig appConfig;

    private static final Pattern QQ_EMAIL_PATTERN = Pattern.compile(Constants.REGEX_QQ_EMAIL);

    @Override
    public void sendEmailCode(String email) {
        // 1. 基本格式校验
        if (!StringUtils.hasText(email)) {
            throw new BaseException("请输入邮箱");
        }
        email = email.trim().toLowerCase();
        if (!QQ_EMAIL_PATTERN.matcher(email).matches()) {
            throw new BaseException("目前只支持QQ邮箱注册");
        }

        // 2. 判重：已注册的邮箱不再发送
        if (userInfoMapper.findByEmail(email) != null) {
            throw new BaseException("邮箱已注册");
        }

        // 3. 60 秒发送间隔限流（防止恶意刷验证码）
        if (!redisComponent.setEmailCodeSendLimit(email)) {
            throw new BaseException("验证码发送过于频繁，请60秒后再试");
        }

        // 4. 生成 6 位数字验证码并存入 Redis（覆盖旧值，失败次数归零）
        String code = RandomStringUtils.randomNumeric(6);
        redisComponent.saveEmailCode(email, code);

        // 5. 发送邮件（失败时清理验证码与限流窗口，允许用户重试）
        try {
            sendMail(email, code);
        } catch (Exception e) {
            redisComponent.cleanEmailCode(email);
            // 限流 key 也一并清掉，否则发信失败后用户要等 60 秒才能重试
            redisComponent.getRedisUtils().delete(Constants.REDIS_KEY_EMAIL_CODE_LIMIT + email);
            log.error("邮箱验证码发送失败, email={}", email, e);
            throw new BaseException("验证码发送失败，请稍后重试");
        }
    }

    @Override
    public void verifyEmailCode(String email, String code) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            throw new BaseException("邮箱验证码不能为空");
        }
        email = email.trim().toLowerCase();

        EmailCodeDto dto = redisComponent.getEmailCode(email);
        if (dto == null || !StringUtils.hasText(dto.getCode())) {
            throw new BaseException("邮箱验证码已过期，请重新发送");
        }

        // 校验通过：立即删除，保证验证码一次性使用
        if (dto.getCode().equals(code.trim())) {
            redisComponent.cleanEmailCode(email);
            return;
        }

        // 校验失败：累加失败次数，达到上限则作废
        int failCount = (dto.getFailCount() == null ? 0 : dto.getFailCount()) + 1;
        if (failCount >= Constants.EMAIL_CODE_MAX_FAIL_COUNT) {
            redisComponent.cleanEmailCode(email);
            throw new BaseException("验证码连续输错次数过多，请重新发送");
        }
        dto.setFailCount(failCount);
        redisComponent.updateEmailCode(email, dto);
        throw new BaseException("邮箱验证码错误，你还有" + (Constants.EMAIL_CODE_MAX_FAIL_COUNT - failCount) + "次机会");
    }

    /**
     * 通过 JavaMailSender 发送 HTML 格式的验证码邮件
     */
    private void sendMail(String email, String code) throws Exception {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(appConfig.getMailFrom());
        helper.setTo(email);
        helper.setSubject("bili-live 注册验证码");
        String html = "<div style='font-family:Arial,sans-serif;line-height:1.8'>"
                + "<p>您好，您正在注册 bili-live 账号。</p>"
                + "<p>本次注册验证码为：<b style='font-size:20px;color:#e60012'>" + code + "</b></p>"
                + "<p>验证码 <b>5 分钟</b>内有效，请勿转发给他人。</p>"
                + "<p style='color:#999'>若非本人操作，请忽略此邮件。</p>"
                + "</div>";
        helper.setText(html, true);
        javaMailSender.send(message);
    }
}
