package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户消息数量统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageCountDto {

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 消息类型描述
     */
    private String messageTypeName;

    /**
     * 未读消息数量
     */
    private Integer messageCount;
}
