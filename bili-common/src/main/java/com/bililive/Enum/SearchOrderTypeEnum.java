package com.bililive.Enum;

/**
 * ES 搜索排序类型枚举
 * 用于 Elasticsearch 视频搜索结果的排序
 */
public enum SearchOrderTypeEnum {
    PLAY_COUNT(0, "playCount", "播放量"),
    CREATE_TIME(1, "createTime", "创建时间"),
    DANMU_COUNT(2, "danmuCount", "弹幕数"),
    COLLECT_COUNT(3, "collectCount", "收藏数");

    private Integer type;
    private String field;
    private String desc;

    SearchOrderTypeEnum(Integer type, String field, String desc) {
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

    /**
     * 根据类型获取枚举
     * @param type 类型值
     * @return 对应的枚举，如果未找到则返回默认值 CREATE_TIME
     */
    public static SearchOrderTypeEnum getByType(Integer type) {
        if (type == null) {
            return CREATE_TIME;
        }
        for (SearchOrderTypeEnum orderTypeEnum : SearchOrderTypeEnum.values()) {
            if (orderTypeEnum.getType().equals(type)) {
                return orderTypeEnum;
            }
        }
        return CREATE_TIME; // 默认按创建时间排序
    }
}
