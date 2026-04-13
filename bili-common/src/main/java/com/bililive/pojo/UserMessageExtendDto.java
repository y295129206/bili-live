package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageExtendDto {
    private String messageContent;
    private String messageContentReply;
    private Integer auditStatus;
}
