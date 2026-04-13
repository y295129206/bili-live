package com.bililive.web.controller;

import com.bililive.Enum.MessageTypeEnum;
import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.Result;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.pojo.UserAction;
import com.bililive.service.UserActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.*;

@RestController
@RequestMapping("/userAction")
@Validated
@Slf4j
public class UserActionController {
    @Autowired
    private UserActionService userActionService;
    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/doAction")
    @GlobalInterceptor(checkLogin = true, messageType = MessageTypeEnum.LIKE)
    public Result loadRecommendVideo(@NotEmpty String videoId, @NotNull Integer actionType,
                                     @Max(2) @Min(1)Integer actionCount, Integer commentId, HttpServletRequest request) {
        UserAction userAction = new UserAction();
        userAction.setVideoId(videoId);
        userAction.setActionType(actionType);
        userAction.setActionCount(actionCount==null?1:actionCount);
        userAction.setCommentId(commentId==null?0:commentId);
        userAction.setUserId(getTokenUserInfoDto( request).getUserId());
        userActionService.saveAction(userAction);
        return Result.success();
    }

    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
