package com.bililive.service.impl;

import com.bililive.global.GlobalException;
import com.bililive.mapper.UserFocusMapper;
import com.bililive.mapper.UserInfoMapper;
import com.bililive.pojo.*;
import com.bililive.service.UserFocusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 用户关注服务实现类
 */
@Service("userFocusService")
@Slf4j
public class UserFocusServiceImpl implements UserFocusService {

    @Autowired
    private UserFocusMapper userFocusMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;


    @Override
    public void focusUser(String userId, String focusUserId) {
        if(userId.equals(focusUserId)){
            throw new BaseException("不能关注自己");
        }
        UserFocus dbuserFocus = userFocusMapper.selectByUserIdAndFocusUserId(userId, focusUserId);
        if(dbuserFocus != null){
            return ;
        }
        UserInfo userInfo =userInfoMapper.findByUserId(focusUserId);
        if(userInfo == null){
            throw new BaseException(600,"用户不存在");
        }
        UserFocus userFocus = new UserFocus();
        userFocus.setUserId(userId);
        userFocus.setFocusUserId(focusUserId);
        userFocus.setFocusTime(new Date());
        userFocusMapper.insert(userFocus);
    }

    @Override
    public void cancelFocusUser(String userId, String focusUserId) {
        if(userId.equals(focusUserId)){
            throw new BaseException("不能取关自己");
        }
        UserFocus dbuserFocus = userFocusMapper.selectByUserIdAndFocusUserId(userId, focusUserId);
        if(dbuserFocus == null){
            return ;
        }
        UserInfo userInfo =userInfoMapper.findByUserId(focusUserId);
        if(userInfo == null){
            throw new BaseException(600,"用户不存在");
        }
        UserFocus userFocus = new UserFocus();
        userFocus.setUserId(userId);
        userFocus.setFocusUserId(focusUserId);
        userFocus.setFocusTime(new Date());
        userFocusMapper.deleteByUserIdAndFocusUserId(userId, focusUserId);
    }

    @Override
    public PaginationResult<UserFocus> findListByPage(UserFocusQuery userFocusQuery) {
        if (userFocusQuery == null) {
            userFocusQuery = new UserFocusQuery();
        }

        // 设置默认分页参数
        if (userFocusQuery.getPageNo() == null || userFocusQuery.getPageNo() < 1) {
            userFocusQuery.setPageNo(1);
        }
        if (userFocusQuery.getPageSize() == null || userFocusQuery.getPageSize() < 1) {
            userFocusQuery.setPageSize(10);
        }

        // 创建 SimplePage 对象用于 MyBatis 分页
        SimplePage simplePage = new SimplePage(userFocusQuery.getPageNo(), userFocusQuery.getPageSize());
        userFocusQuery.setSimplePage(simplePage);
        
        // 查询数据列表
        List<UserFocus> list = userFocusMapper.selectList(userFocusQuery);
        
        // 查询总数（需要清除分页参数）
        userFocusQuery.setSimplePage(null);
        Integer totalCount = userFocusMapper.selectCount(userFocusQuery);
        
        // 重新设置分页参数
        userFocusQuery.setSimplePage(simplePage);
        
        // 计算总页数
        int totalPages = (totalCount + userFocusQuery.getPageSize() - 1) / userFocusQuery.getPageSize();

        PaginationResult<UserFocus> paginationResult = new PaginationResult<>();
        paginationResult.setList(list);
        paginationResult.setTotalCount(totalCount);
        paginationResult.setTotalPages(totalPages);
        paginationResult.setPageNo(userFocusQuery.getPageNo());
        paginationResult.setPageSize(userFocusQuery.getPageSize());
        // 构建分页结果
        return paginationResult;
    }

}
