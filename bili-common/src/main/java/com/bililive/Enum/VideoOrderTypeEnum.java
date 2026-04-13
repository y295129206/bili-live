package com.bililive.Enum;

public enum VideoOrderTypeEnum {
    CREATE_TIME(0, "create_time", "创建时间"),
    PLAY_COUNT(1, "play_count", "最多播放量"),
    COLLECT_COUNT(2, "collect_count", "最多收藏数");

    private Integer type;
    private String field;
    private String desc;

    VideoOrderTypeEnum(Integer type, String field, String desc) {
        this.type = type;
        this.field = field;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getField() {
        return field;
    }

    public String getDesc() {
        return desc;
    }

    public static VideoOrderTypeEnum getByType(Integer type) {
        for (VideoOrderTypeEnum orderTypeEnum : VideoOrderTypeEnum.values()) {
            if (orderTypeEnum.getType().equals(type)) {
                return orderTypeEnum;
            }
        }
        return CREATE_TIME; // 默认按创建时间排序
    }
}
