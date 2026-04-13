package com.bililive.service.impl;

import com.bililive.Enum.UserSexEnum;
import com.bililive.Enum.UserStatusEnum;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.mapper.UserFocusMapper;
import com.bililive.mapper.UserInfoMapper;
import com.bililive.mapper.VideoInfoMapper;
import com.bililive.pojo.*;
import com.bililive.service.UserInfoService;
import com.bililive.utils.CopyTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;

@Service
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserFocusMapper userFocusMapper;
    @Autowired
    private VideoInfoMapper videoInfoMapper;

    @Override
    public Boolean register(String email, String nickName, String registerPassword) {
        UserInfo userInfo = userInfoMapper.findByEmail(email);
        if (userInfo == null) {
            UserInfo nickNameUser = userInfoMapper.findByNickName(nickName);
            if (nickNameUser == null) {
                UserInfo user = new UserInfo();
                //生成随机用户号，长度。字母。数字
                String user_id = RandomStringUtils.random(Constants.LENGTH_10,false,true);
                user.setNickName(nickName);
                user.setEmail(email);
                user.setUserId(user_id);
                String password = DigestUtils.md5DigestAsHex(registerPassword.getBytes()).toUpperCase();
                user.setPassword(password);
                user.setJoinTime(new Date());   //new Date()返回当前时间
                user.setStatus(UserStatusEnum.ENABLE.getStatus());
                user.setSex(UserSexEnum.SECRECY.getSex());
                user.setTheme(Constants.ONE);
                SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
                user.setCurrentCoinCount(sysSettingDto.getRegisterCoinCount());
                user.setTotalCoinCount(sysSettingDto.getRegisterCoinCount());

                userInfoMapper.insert(user);
                return true;
            }else{
                throw new BaseException("用户名已注册");
            }
        }else{
            throw new BaseException("邮箱已注册");
        }
    }

    @Override
    public TokenUserInfoDto login(String email, String password, String ip) {
        UserInfo userInfo = userInfoMapper.findByEmail(email);
        if (userInfo == null) {
            throw new BaseException("邮箱未注册");
        }
        if(!userInfo.getPassword().equals(password)){
            throw new BaseException("密码错误");
        }
        if(userInfo.getStatus().equals(UserStatusEnum.DISABLE.getStatus())){
            throw new BaseException("账户已禁用");
        }
        
        // 更新登录信息
        UserInfo updateUser = new UserInfo();
        updateUser.setUserId(userInfo.getUserId());  // 设置用户 ID（用于更新）
        updateUser.setLastLoginTime(new Date());
        updateUser.setLastLoginIp(ip);
        userInfoMapper.updateByUserId(updateUser);
        
        // 复制用户信息到 TokenUserInfoDto
        TokenUserInfoDto tokenUserInfoDto = CopyTools.copy(userInfo, TokenUserInfoDto.class);
        redisComponent.saveToken(tokenUserInfoDto);
        return tokenUserInfoDto;
    }

    @Override
    public UserInfo getUserDetailInfo(String currentUserId,String userId) {
        UserInfo userInfo = userInfoMapper.findByUserId(userId);
        if (userInfo == null) {
            throw new BaseException(404,"用户不存在");
        }
        UserCountInfoDto countInfoDto = videoInfoMapper.selectSumCountInfo(userId);
        userInfo.setPlayCount(countInfoDto.getPlayCount());
        userInfo.setLikeCount(countInfoDto.getLikeCount());
        Integer fansCount = userFocusMapper.selectFansCount(userId);
        userInfo.setFansCount(fansCount);
        Integer focusCount = userFocusMapper.selectFocusCount(userId);
        userInfo.setFocusCount(focusCount);
        if(currentUserId == null){
            userInfo.setHaveFocus(false);
        }else{
            UserFocus userFocus = userFocusMapper.selectByUserIdAndFocusUserId(currentUserId, userId);
            userInfo.setHaveFocus(userFocus == null ? false : true);
        }
        return userInfo;
    }

    @Override
    public void updateUserInfo(UserInfo userInfo, TokenUserInfoDto tokenUserInfoDto) {
        UserInfo dbuserInfo = userInfoMapper.findByUserId(userInfo.getUserId());
        if (!dbuserInfo.getNickName().equals(userInfo.getNickName())&&dbuserInfo.getCurrentCoinCount()<Constants.UPDATE_NICK_NAME_COIN) {
            throw new BaseException("硬币不足,无法修改昵称");
        }
        if(!dbuserInfo.getNickName().equals(userInfo.getNickName())){
            Integer count = userInfoMapper.updateCoinCountByUserId(userInfo.getUserId(), -Constants.UPDATE_NICK_NAME_COIN);
        }
        userInfoMapper.updateByUserId(userInfo);

        Boolean updateTokenInfo = false;
        if(!userInfo.getAvatar().equals(tokenUserInfoDto.getAvatar())){
            tokenUserInfoDto.setAvatar(userInfo.getAvatar());
            updateTokenInfo = true;
        }
        if(!tokenUserInfoDto.getNickName().equals(userInfo.getNickName())){
            tokenUserInfoDto.setNickName(userInfo.getNickName());
            updateTokenInfo = true;
        }
        if(updateTokenInfo){
            redisComponent.updateToken(tokenUserInfoDto);
        }

    }
    @Override
    public void updateUserInfoByUserId(UserInfo userInfo){
        userInfoMapper.updateByUserId(userInfo);
    }

    @Override
    public UserCountInfoDto getUserCountInfo(String userId) {
        UserInfo userInfo = userInfoMapper.findByUserId(userId);
        Integer fansCount = userFocusMapper.selectFansCount(userId);
        Integer focusCount = userFocusMapper.selectFocusCount(userId);
        UserCountInfoDto userCountInfoDto = new UserCountInfoDto();
        userCountInfoDto.setFansCount(fansCount);
        userCountInfoDto.setFocusCount(focusCount);
        userCountInfoDto.setCurrentCoinCount(userInfo.getCurrentCoinCount());
        return userCountInfoDto;
    }

    @Override
    public Integer getPreDayUserCount(String preDate) {
        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setJoinTime(preDate);
        return userInfoMapper.selectCount(userInfoQuery);
    }

    @Override
    public java.util.List<UserInfo> findListByParam(UserInfoQuery query) {
        if (query == null) {
            query = new UserInfoQuery();
        }
        
        // 设置默认分页参数
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }
        
        // 创建 SimplePage 对象用于 MyBatis 分页
        SimplePage simplePage = new SimplePage(query.getPageNum(), query.getPageSize());
        query.setSimplePage(simplePage);
        
        return userInfoMapper.selectList(query);
    }

    @Override
    public PaginationResult<UserInfo> findListByPage(UserInfoQuery query) {
        if (query == null) {
            query = new UserInfoQuery();
        }

        // 设置默认分页参数
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }

        // 创建 SimplePage 对象用于 MyBatis 分页
        SimplePage simplePage = new SimplePage(query.getPageNum(), query.getPageSize());
        query.setSimplePage(simplePage);
        
        // 查询数据列表
        java.util.List<UserInfo> list = userInfoMapper.selectList(query);
        
        // 查询总数（需要清除分页参数）
        query.setSimplePage(null);
        Integer totalCount = userInfoMapper.selectCount(query);
        
        // 重新设置分页参数
        query.setSimplePage(simplePage);
        
        // 计算总页数
        int totalPages = (totalCount + query.getPageSize() - 1) / query.getPageSize();

        PaginationResult<UserInfo> paginationResult = new PaginationResult<>();
        paginationResult.setList(list);
        paginationResult.setTotalCount(totalCount);
        paginationResult.setTotalPages(totalPages);
        paginationResult.setPageNo(query.getPageNum());
        paginationResult.setPageSize(query.getPageSize());
        
        return paginationResult;
    }

    @Override
    public void changeStatus(String userId, Integer status) {
        if (userId == null || userId.isEmpty()) {
            throw new BaseException("用户ID不能为空");
        }
        if (status == null) {
            throw new BaseException("状态值不能为空");
        }
        
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setStatus(status);
        userInfoMapper.updateByUserId(userInfo);
    }
}
