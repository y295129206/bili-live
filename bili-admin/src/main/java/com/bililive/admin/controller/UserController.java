package com.bililive.admin.controller;

import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.Result;
import com.bililive.pojo.UserInfo;
import com.bililive.pojo.UserInfoQuery;
import com.bililive.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RequestMapping("/user")
@RestController
@Validated
@Slf4j
public class UserController {
    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/loadUser")
    public Result<PaginationResult<UserInfo>> loadUser(UserInfoQuery  query) {
        query.setOrderBy("join_time desc");
        PaginationResult<UserInfo> userInfo = userInfoService.findListByPage(query);
        return Result.success(userInfo);
    }

    @RequestMapping("/changeStatus")
    public Result<String> changeStatus(@NotNull String userId, @NotNull Integer status) {
        userInfoService.changeStatus(userId, status);
        return Result.success("修改成功");
    }
}
