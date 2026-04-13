package com.bililive.Enum;

/**
 * 用户消息类型枚举
 */
public enum MessageTypeEnum {
    NONE(0, "无"),
    SYSTEM(1, "系统消息"),
    LIKE(2, "点赞"),
    COLLECT(3, "收藏"),
    COMMENT(4, "评论");

    private Integer type;
    private String desc;

    MessageTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 根据类型获取枚举
     * @param type 消息类型
     * @return 对应的枚举值，未找到返回null
     */
    public static MessageTypeEnum getByType(Integer type) {
        if (type == null) {
            return null;
        }
        for (MessageTypeEnum item : MessageTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
