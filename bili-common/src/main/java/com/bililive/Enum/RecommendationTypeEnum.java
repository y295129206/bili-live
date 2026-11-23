package com.bililive.Enum;

/**
 * 推荐类型枚举
 */
public enum RecommendationTypeEnum {
    
    USER_CF(1, "基于用户的协同过滤"),
    ITEM_CF(2, "基于物品的协同过滤"),
    CONTENT_BASED(3, "基于内容的推荐"),
    HYBRID(4, "混合推荐"),
    HOT(5, "热门推荐"),
    COLD_START(6, "冷启动推荐");

    private Integer type;
    private String desc;

    RecommendationTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static RecommendationTypeEnum getByType(Integer type) {
        for (RecommendationTypeEnum item : RecommendationTypeEnum.values()) {
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
}
