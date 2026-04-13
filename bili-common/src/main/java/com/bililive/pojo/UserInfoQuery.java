package com.bililive.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * 用户信息查询条件封装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码 (默认第 1 页)
     */
    private Integer pageNum = 1;

    /**
     * 每页显示条数 (默认 10 条)
     */
    private Integer pageSize = 10;

    /**
     * 用户 ID (精确匹配)
     */
    private String userId;

    /**
     * 昵称 (支持模糊查询)
     */
    private String nickName;

    /**
     * 邮箱 (支持模糊查询)
     */
    private String email;

    /**
     * 性别：0 女 1 男 2 未知
     */
    private Integer sex;

    /**
     * 学校 (支持模糊查询)
     */
    private String school;

    /**
     * 用户状态：0 禁用 1 正常
     */
    private Integer status;

    /**
     * 排序字段 (动态排序用，例如 "join_time", "last_login_time")
     */
    private String orderBy;

    /**
     * 分页参数（用于 MyBatis）
     */
    private SimplePage simplePage;

    private List<String> userIdList;

    private String joinTime;

}
