package com.bililive.web.controller;

import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.Enum.VideoOrderTypeEnum;
import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.service.UserActionService;
import com.bililive.service.UserFocusService;
import com.bililive.service.UserInfoService;
import com.bililive.service.VideoInfoService;
import com.bililive.utils.CopyTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.*;
import java.util.List;

@RestController
@RequestMapping("/uhome")
@Slf4j
@Validated
public class UHomeController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private VideoInfoService videoInfoService;
    @Autowired
    private UserFocusService userFocusService;
    @Autowired
    private UserActionService userActionService;
    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/getUserInfo")
    public Result<UserInfoVO> getUserInfo(@NotEmpty String userId, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserInfo userInfo = userInfoService.getUserDetailInfo(tokenUserInfoDto==null?null:tokenUserInfoDto.getUserId(), userId);

        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        return Result.success(userInfoVO);
    }
    @RequestMapping("/updateUserInfo")
    @GlobalInterceptor(checkLogin = true)
    public Result updateUserInfo(@NotEmpty @Size(max = 20) String nickName,
                                 @NotEmpty @Size(max = 100) String avatar,
                                 @NotNull Integer sex,
                                 @Size(max = 10) String birthday,
                                 @Size(max = 150) String school,
                                 @Size(max = 80) String personIntroduction ,
                                 @Size(max = 300) String noticeInfo,
                                 HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setNickName(nickName);
        userInfo.setAvatar(avatar);
        userInfo.setSex(sex);
        userInfo.setBirthday(birthday);
        userInfo.setSchool(school);
        userInfo.setPersonIntroduce(personIntroduction);
        userInfo.setNoticeInfo(noticeInfo);
        userInfoService.updateUserInfo(userInfo, tokenUserInfoDto);
        return Result.success();
    }

    @RequestMapping("/saveTheme")
    @GlobalInterceptor(checkLogin = true)
    public Result saveTheme(@NotNull @Max(10) @Min(1) Integer theme, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserInfo userInfo = new UserInfo();
        userInfo.setTheme(theme);
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfoService.updateUserInfoByUserId(userInfo);
        return Result.success();
    }

    @RequestMapping("/focus")
    @GlobalInterceptor(checkLogin = true)
    public Result focus(@NotEmpty String focusUserId, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        userFocusService.focusUser(tokenUserInfoDto.getUserId(), focusUserId);
        return Result.success();
    }

    @RequestMapping("/cancelFocus")
    @GlobalInterceptor(checkLogin = true)
    public Result cancelFocus(@NotEmpty String focusUserId, HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        userFocusService.cancelFocusUser(tokenUserInfoDto.getUserId(), focusUserId);
        return Result.success();
    }

    @RequestMapping("/loadFocusList")
    @GlobalInterceptor(checkLogin = true)
    public Result<PaginationResult<UserFocus>> loadFocusList(Integer pageNo,HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setUserId(tokenUserInfoDto.getUserId());
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setOrderBy("focus_time desc");
        userFocusQuery.setQueryType(Constants.ZERO);
        PaginationResult<UserFocus> result = userFocusService.findListByPage(userFocusQuery);
        return Result.success(result);
    }
    @RequestMapping("/loadFansList")
    @GlobalInterceptor(checkLogin = true)
    public Result<PaginationResult<UserFocus>> loadFansList(Integer pageNo,HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setUserId(tokenUserInfoDto.getUserId());
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setOrderBy("focus_time desc");
        userFocusQuery.setQueryType(Constants.ONE);
        PaginationResult<UserFocus> result = userFocusService.findListByPage(userFocusQuery);
        return Result.success(result);
    }

    @RequestMapping("/loadVideoList")
    public Result<PaginationResult<VideoInfo>> loadVideoList(@NotEmpty String userId,Integer type, Integer pageNo,String videoName,Integer orderType) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if(type!=null){
            videoInfoQuery.setPageSize(15);
        }
        VideoOrderTypeEnum videoOrderTypeEnum = VideoOrderTypeEnum.getByType(orderType);
        if(videoOrderTypeEnum==null){
            videoOrderTypeEnum = VideoOrderTypeEnum.CREATE_TIME;
        }
        videoInfoQuery.setOrderBy(videoOrderTypeEnum.getField()+" desc");
        videoInfoQuery.setVideoNameFuzzy(videoName);
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setPageNo(pageNo);
        PaginationResult<VideoInfo> result = videoInfoService.findListByPage(videoInfoQuery);
        return Result.success(result);
    }
    @RequestMapping("/loadUserCollection")
    public Result<PaginationResult<UserAction>> loadUserCollection(@NotEmpty String userId,Integer pageNo,HttpServletRequest request) {
        UserActionQuery userActionQuery = new UserActionQuery();
        userActionQuery.setUserId(userId);
        userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.VIDEO_COLLECT.getType()});
        userActionQuery.setPageNo(pageNo);
        userActionQuery.setQueryVideoInfo(true);
        userActionQuery.setOrderBy("action_time desc");
        PaginationResult<UserAction> result = userActionService.findListByPage(userActionQuery);
        return Result.success(result);
    }
    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
