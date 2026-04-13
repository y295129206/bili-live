package com.bililive.service;

import com.bililive.pojo.UserFocus;
import com.bililive.pojo.UserFocusQuery;
import com.bililive.pojo.PaginationResult;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 用户关注服务接口
 */
public interface UserFocusService {


    void focusUser(String userId, @NotEmpty String focusUserId);

    void cancelFocusUser(String userId, @NotEmpty String focusUserId);

    PaginationResult<UserFocus> findListByPage(UserFocusQuery userFocusQuery);
}
