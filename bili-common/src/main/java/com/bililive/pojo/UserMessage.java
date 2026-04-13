package com.bililive.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import com.bililive.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户消息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID自增
     */
    private Integer messageId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 主体ID(视频ID)
     */
    private String videoId;

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 发送人ID
     */
    private String sendUserId;

    /**
     * 阅读状态 0:未读 1:已读
     */
    private Integer readType;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 扩展信息(JSON格式)
     */
    private String extendJson;

    private String sendUserAvatar;

    private String sendUserName;

    private String videoName;

    private String videoCover;

    private UserMessageExtendDto extendDto;

    public UserMessageExtendDto getExtendDto() {
    	return extendJson == null ? new UserMessageExtendDto() : JsonUtils.convertJson2Obj(extendJson, UserMessageExtendDto.class);
    }
}
