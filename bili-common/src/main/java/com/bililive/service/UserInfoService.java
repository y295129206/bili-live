package com.bililive.service;

import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.pojo.UserCountInfoDto;
import com.bililive.pojo.UserInfo;
import com.bililive.pojo.UserInfoQuery;

public interface UserInfoService {
    Boolean register(String email,String nickName,String registerPassword);

    TokenUserInfoDto login(String email, String password, String ip);

    UserInfo getUserDetailInfo(String currentUserId, String userId);

    void updateUserInfo(UserInfo userInfo, TokenUserInfoDto tokenUserInfoDto);

    void updateUserInfoByUserId(UserInfo userInfo);

    UserCountInfoDto getUserCountInfo(String userId);

    Integer getPreDayUserCount(String preDate);

    /**
     * 根据查询条件获取用户列表（不分页）
     * @param query 查询参数
     * @return 用户信息列表
     */
    java.util.List<UserInfo> findListByParam(UserInfoQuery query);

    /**
     * 根据查询条件分页获取用户列表
     * @param query 查询参数
     * @return 分页结果
     */
    PaginationResult<UserInfo> findListByPage(UserInfoQuery query);

    /**
     * 修改用户状态
     * @param userId 用户ID
     * @param status 状态值
     */
    void changeStatus(String userId, Integer status);
}
