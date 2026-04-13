package com.bililive.web.controller;

import com.bililive.Enum.MessageTypeEnum;
import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.service.UserActionService;
import com.bililive.service.VideoCommentService;
import com.bililive.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/comment")
@Validated
public class VideoCommentController {
    @Autowired
    private VideoCommentService videoCommentService;
    @Autowired
    private VideoInfoService videoInfoService;
    @Autowired
    private UserActionService userActionService;
    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/postComment")
    @GlobalInterceptor(checkLogin = true, messageType = MessageTypeEnum.COMMENT)
    public Result<String> postComment(@NotEmpty String videoId,
                                      @NotEmpty @Size(max = 500) String content,
                                      Integer replyCommentId,
                                      @Size (max = 50) String imgPath,
                                      HttpServletRequest  request){
        VideoComment comment = new VideoComment();
        comment.setVideoId(videoId);
        comment.setUserId(getTokenUserInfoDto( request).getUserId());
        comment.setAvatar(getTokenUserInfoDto( request).getAvatar());
        comment.setNickName(getTokenUserInfoDto( request).getNickName());
        comment.setContent(content);
        comment.setImgPath(imgPath);
        videoCommentService.postComment(comment,replyCommentId);
        comment.setReplyAvatar(getTokenUserInfoDto(request).getAvatar());
        return Result.success(null);
    }
    @RequestMapping("/loadComment")
    public Result<VideoCommentResponse> loadComment(@NotEmpty String videoId,
                                                    Integer pageNo,
                                                    Integer orderType,
                                                    HttpServletRequest request){
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if(videoInfo.getInteraction()!=null&&videoInfo.getInteraction().contains("0")){
            return Result.success();
        }
        VideoCommentQuery query = new VideoCommentQuery();
        query.setVideoId(videoId);
        query.setPageNo(pageNo);
        query.setPageSize(15);
        query.setPCommentId(0);
        query.setLoadChildren(true);
        String orderBy = orderType==null||orderType==0?"like_count desc,comment_id desc":"comment_id desc";
        query.setOrderBy(orderBy);
        PaginationResult<VideoComment> paginationResult = videoCommentService.findListByPage(query);

        if(pageNo==1){
            List<VideoComment> topCommentList = topComment(videoId);
            if(!topCommentList.isEmpty()){
                List<VideoComment> commentList = paginationResult.getList().stream().filter(
                        comment ->!comment.getCommentId().equals(topCommentList.get(0).getCommentId())).collect(Collectors.toList());
                commentList.addAll(0,topCommentList);
                paginationResult.setList(commentList);
            }
        }
        VideoCommentResponse videoCommentResponse = new VideoCommentResponse();
        videoCommentResponse.setCommentData(paginationResult);
        List<UserAction> userActionList = new ArrayList<>();
        if(getTokenUserInfoDto( request)!=null){
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setUserId(getTokenUserInfoDto( request).getUserId());
            userActionQuery.setVideoId(videoId);
            userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.COMMENT_LIKE.getType(),UserActionTypeEnum.COMMENT_HATE.getType()});
            userActionList = userActionService.findListByParam(userActionQuery);
        }
        videoCommentResponse.setUserActionList(userActionList);
        return Result.success(videoCommentResponse);
    }
    @RequestMapping("/topComment")
    @GlobalInterceptor(checkLogin = true)
    public Result topComment(@NotNull Integer commentId,HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        videoCommentService.topComment(commentId,tokenUserInfoDto.getUserId());
        return Result.success();
    }
    @RequestMapping("/cancelTopComment")
    @GlobalInterceptor(checkLogin = true)
    public Result cancelTopComment(@NotNull Integer commentId,HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        videoCommentService.cancelTopComment(commentId,tokenUserInfoDto.getUserId());
        return Result.success();
    }
    @RequestMapping("/userDelComment")
    @GlobalInterceptor(checkLogin = true)
    public Result userDelComment(@NotNull Integer commentId,HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        videoCommentService.deleteComment(commentId,tokenUserInfoDto.getUserId());
        return Result.success();
    }

    private List<VideoComment> topComment(String videoId){
        VideoCommentQuery query = new VideoCommentQuery();
        query.setVideoId(videoId);
        query.setTopType(1);
        query.setPCommentId(0);
        query.setLoadChildren(true);
        List<VideoComment> topCommentList = videoCommentService.findListByParam(query);
        return topCommentList;
    }
    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }

}
