package com.bililive.Enum;

public enum UserSexEnum {
    SECRECY(0,"保密"),
    MAN(1,"男"),
    WOMAN(2,"女");
    private Integer sex;
    private String desc;

    UserSexEnum(Integer sex, String desc) {
        this.sex = sex;
        this.desc = desc;
    }
    public static UserSexEnum  getBySex(Integer sex){
        for(UserSexEnum sexEnum:UserSexEnum.values()){
            if(sexEnum.getSex().equals(sex)){
                return sexEnum;
            }
        }
        return null;
    }

    public Integer getSex() {
        return sex;
    }
    public void getDesc(String desc) {
        this.desc = desc;
    }
    public void setDesc(String desc) {this.desc = desc;}
}
