package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 邮箱验证码缓存对象（存于 Redis）
 *
 * 将验证码与失败次数封装在一个对象里，一次读取即可拿到全部状态，
 * 避免"验证码"和"失败计数"用两个 key 带来的往返与不一致问题。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailCodeDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 6 位数字验证码 */
    private String code;

    /** 连续输错次数，达到上限（Constants.EMAIL_CODE_MAX_FAIL_COUNT）后验证码作废 */
    private Integer failCount;

    public EmailCodeDto(String code) {
        this.code = code;
        this.failCount = 0;
    }
}
