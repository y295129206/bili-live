package com.bililive.Enum;

public enum VideoRecommendTypeEnum {
    NOT_RECOMMEND(0, "未推荐"),
    RECOMMEND(1, "已推荐");

    private Integer type;
    private String desc;

    VideoRecommendTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static VideoRecommendTypeEnum getByType(Integer type) {
        for (VideoRecommendTypeEnum recommendTypeEnum : VideoRecommendTypeEnum.values()) {
            if (recommendTypeEnum.getType().equals(type)) {
                return recommendTypeEnum;
            }
        }
        return null;
    }
}
