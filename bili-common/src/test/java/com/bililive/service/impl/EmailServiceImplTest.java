package com.bililive.service.impl;

import com.bililive.component.RedisComponent;
import com.bililive.entity.config.AppConfig;
import com.bililive.entity.constants.Constants;
import com.bililive.mapper.UserInfoMapper;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.EmailCodeDto;
import com.bililive.pojo.UserInfo;
import com.bililive.redis.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EmailServiceImpl 单元测试（教学示例 · 第二课 Mockito 打桩）
 *
 * 【为什么要 Mock？】
 * EmailServiceImpl 依赖 4 个外部对象：
 *   - JavaMailSender：真实发信会连 QQ SMTP，慢且需要网络/凭据
 *   - RedisComponent：依赖 Redis 服务
 *   - UserInfoMapper：依赖 MySQL
 *   - AppConfig：配置对象
 * 单元测试要"快、独立、可重复"，不能真的连这些外部系统。
 * 用 @Mock 造出这些对象的"替身"，只关注 EmailServiceImpl 自身的逻辑。
 *
 * 【核心注解】
 *   @ExtendWith(MockitoExtension.class)：启用 Mockito 注解支持
 *   @Mock：创建虚拟依赖对象
 *   @InjectMocks：创建被测对象，并自动把 @Mock 注入进去
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("邮箱验证码服务 单元测试（Mockito）")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private RedisComponent redisComponent;
    @Mock
    private UserInfoMapper userInfoMapper;
    @Mock
    private AppConfig appConfig;
    @Mock
    private RedisUtils redisUtils;

    @InjectMocks
    private EmailServiceImpl emailService;

    private static final String QQ_EMAIL = "123456789@qq.com";

    /**
     * 只给"真正会发信"的用例打桩。
     *
     * 【教学要点】Mockito 默认 STRICT_STUBS 严格模式：任何打桩如果本次用例没有用到，
     * 就会抛 UnnecessaryStubbingException。因此不要像 Here 这样在 @BeforeEach 里
     * 对所有用例统一打桩 —— 校验类用例根本不发信，用不到这些桩。
     * 正确做法：按需打桩，这里封装成方法由发信用例自行调用。
     */
    private void stubMailSender() {
        // createMimeMessage() 默认返回 null，打桩返回一个不依赖网络的真实对象
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((javax.mail.Session) null));
        when(appConfig.getMailFrom()).thenReturn("2918971631@qq.com");
    }

    // ========== sendEmailCode ==========

    @Test
    @DisplayName("发送成功：合法QQ邮箱、未注册、未限流 → 存验证码并发信")
    void sendEmailCodeSuccess() {
        stubMailSender();
        when(userInfoMapper.findByEmail(QQ_EMAIL)).thenReturn(null);
        when(redisComponent.setEmailCodeSendLimit(QQ_EMAIL)).thenReturn(true);

        emailService.sendEmailCode(QQ_EMAIL);

        // 验证：存了一次验证码
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisComponent).saveEmailCode(eq(QQ_EMAIL), codeCaptor.capture());
        assertEquals(6, codeCaptor.getValue().length());
        assertTrue(codeCaptor.getValue().matches("\\d{6}"), "验证码必须是6位数字");
        // 验证：调用了一次真实发信
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("邮箱已注册：不存码、不发信，抛异常")
    void sendEmailCodeRegisteredEmail() {
        when(userInfoMapper.findByEmail(QQ_EMAIL)).thenReturn(new UserInfo());

        BaseException ex = assertThrows(BaseException.class,
                () -> emailService.sendEmailCode(QQ_EMAIL));
        assertEquals("邮箱已注册", ex.getMsg());

        verify(redisComponent, never()).saveEmailCode(anyString(), anyString());
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("非QQ邮箱：直接拒绝，不查库、不发信")
    void sendEmailCodeRejectNonQQEmail() {
        BaseException ex = assertThrows(BaseException.class,
                () -> emailService.sendEmailCode("someone@gmail.com"));
        assertEquals("目前只支持QQ邮箱注册", ex.getMsg());

        verifyNoInteractions(userInfoMapper, javaMailSender);
    }

    @Test
    @DisplayName("60秒限流：窗口未过 → 拒绝发送")
    void sendEmailCodeRateLimited() {
        when(userInfoMapper.findByEmail(QQ_EMAIL)).thenReturn(null);
        when(redisComponent.setEmailCodeSendLimit(QQ_EMAIL)).thenReturn(false);

        BaseException ex = assertThrows(BaseException.class,
                () -> emailService.sendEmailCode(QQ_EMAIL));
        assertTrue(ex.getMsg().contains("频繁"));

        verify(redisComponent, never()).saveEmailCode(anyString(), anyString());
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("发信失败：清理验证码与限流窗口，允许重试")
    void sendEmailCodeMailFailureCleansUp() {
        stubMailSender();
        when(userInfoMapper.findByEmail(QQ_EMAIL)).thenReturn(null);
        when(redisComponent.setEmailCodeSendLimit(QQ_EMAIL)).thenReturn(true);
        doThrow(new RuntimeException("SMTP 连接失败"))
                .when(javaMailSender).send(any(MimeMessage.class));
        when(redisComponent.getRedisUtils()).thenReturn(redisUtils);

        BaseException ex = assertThrows(BaseException.class,
                () -> emailService.sendEmailCode(QQ_EMAIL));
        assertTrue(ex.getMsg().contains("发送失败"));

        verify(redisComponent).cleanEmailCode(QQ_EMAIL);
        verify(redisUtils).delete(Constants.REDIS_KEY_EMAIL_CODE_LIMIT + QQ_EMAIL);
    }

    // ========== verifyEmailCode ==========

    @Test
    @DisplayName("校验一致：验证码正确 → 通过后立即删除（一次性）")
    void verifyEmailCodeSuccessDeletesCode() {
        when(redisComponent.getEmailCode(QQ_EMAIL)).thenReturn(new EmailCodeDto("123456"));

        assertDoesNotThrow(() -> emailService.verifyEmailCode(QQ_EMAIL, "123456"));

        verify(redisComponent).cleanEmailCode(QQ_EMAIL);
        verify(redisComponent, never()).updateEmailCode(anyString(), any());
    }

    @Test
    @DisplayName("验证码不存在：抛过期异常")
    void verifyEmailCodeExpired() {
        when(redisComponent.getEmailCode(QQ_EMAIL)).thenReturn(null);

        BaseException ex = assertThrows(BaseException.class,
                () -> emailService.verifyEmailCode(QQ_EMAIL, "123456"));
        assertTrue(ex.getMsg().contains("过期"));
    }

    @Test
    @DisplayName("校验不一致：失败次数 0→1，验证码保留")
    void verifyEmailCodeWrongIncrementsFailCount() {
        EmailCodeDto dto = new EmailCodeDto("123456");
        when(redisComponent.getEmailCode(QQ_EMAIL)).thenReturn(dto);

        BaseException ex = assertThrows(BaseException.class,
                () -> emailService.verifyEmailCode(QQ_EMAIL, "654321"));
        assertTrue(ex.getMsg().contains("4次机会"), "应提示剩余次数");

        ArgumentCaptor<EmailCodeDto> captor = ArgumentCaptor.forClass(EmailCodeDto.class);
        verify(redisComponent).updateEmailCode(eq(QQ_EMAIL), captor.capture());
        assertEquals(1, captor.getValue().getFailCount());
        verify(redisComponent, never()).cleanEmailCode(anyString());
    }

    @Test
    @DisplayName("失败达上限：第5次输错 → 验证码作废并删除")
    void verifyEmailCodeMaxFailDeletesCode() {
        EmailCodeDto dto = new EmailCodeDto("123456");
        dto.setFailCount(4); // 已经错了4次，这是第5次
        when(redisComponent.getEmailCode(QQ_EMAIL)).thenReturn(dto);

        BaseException ex = assertThrows(BaseException.class,
                () -> emailService.verifyEmailCode(QQ_EMAIL, "000000"));
        assertTrue(ex.getMsg().contains("次数过多"));

        verify(redisComponent).cleanEmailCode(QQ_EMAIL);
        verify(redisComponent, never()).updateEmailCode(anyString(), any());
    }
}
