package com.bililive.Enum;

/**
 * 统计类型枚举
 */
public enum StatisticsTypeEnum {
    
    PLAY(0, "播放量"),
    FANS(1, "粉丝数"),
    LIKE(2, "点赞数"),
    COLLECTION(3, "收藏数"),
    COIN(4, "投币数"),
    COMMENT(5, "评论数"),
    DANMU(6, "弹幕数");

    private Integer type;
    private String desc;

    StatisticsTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static StatisticsTypeEnum getByType(Integer type) {
        for (StatisticsTypeEnum item : StatisticsTypeEnum.values()) {
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
