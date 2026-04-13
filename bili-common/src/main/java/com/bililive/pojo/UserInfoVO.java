package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 0:女 1:男 2:保密
     */
    private Integer sex;
    /**
     * 个人简介
     */
    private String personIntroduction;
    /**
     * 空间公告
     */
    private String noticeInfo;
    /**
     * 等级
     */
    private Integer grade;
    /**
     * 出生日期
     */
    private String birthday;
    /**
     * 学校
     */
    private String school;
    /**
     * 关注数
     */
    private Integer fansCount=0;
    /**
     * 粉丝数
     */
    private Integer focusCount=0;
    /**
     * 点赞数
     */
    private Integer likeCount=0;
    /**
     * 播放数
     */
    private Integer playCount=0;
    /**
     * 是否已关注
     */
    private Boolean haveFocus;
    /**
     * 主题
     */
    private Integer theme;
}
