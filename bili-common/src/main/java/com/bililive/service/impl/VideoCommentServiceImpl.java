package com.bililive.service.impl;

import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.mapper.UserActionMapper;
import com.bililive.mapper.UserInfoMapper;
import com.bililive.mapper.VideoInfoMapper;
import com.bililive.pojo.*;
import com.bililive.mapper.VideoCommentMapper;
import com.bililive.service.UserInfoService;
import com.bililive.service.VideoCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 视频评论服务实现类
 */
@Service
@Slf4j
public class VideoCommentServiceImpl implements VideoCommentService {

    @Autowired
    private VideoCommentMapper videoCommentMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private VideoInfoMapper videoInfoMapper;
    @Autowired
    private UserActionMapper userActionMapper;

    @Override
    public void postComment(VideoComment videoComment, Integer replyCommentId) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoComment.getVideoId());
        if(videoInfo == null){
            throw new BaseException(600,"视频不存在");
        }
        if(videoInfo.getInteraction()!=null&&videoInfo.getInteraction().contains("0")){
            throw new BaseException(600,"视频不允许评论");
        }
        if(replyCommentId!= null){
            VideoComment replyComment = videoCommentMapper.selectByCommentId(replyCommentId);
            if(replyComment == null||!replyComment.getVideoId().equals(videoComment.getVideoId())){
                throw new BaseException(600, "评论不存在");
            }
            if(replyComment.getPCommentId()==0){
                videoComment.setPCommentId(replyComment.getCommentId());
            }else{
                videoComment.setPCommentId(replyComment.getPCommentId());
                videoComment.setReplyUserId(replyComment.getUserId());
            }
            UserInfo userInfo = userInfoMapper.findByUserId(replyComment.getUserId());
            videoComment.setReplyNickName(userInfo.getNickName());
            videoComment.setReplyAvatar(userInfo.getAvatar());
        }else{
            videoComment.setPCommentId(0);
        }
        videoComment.setPostTime(new Date());
        videoComment.setVideoUserId(videoInfo.getUserId());
        videoCommentMapper.insert(videoComment);
        UserAction userAction = new UserAction();
        userAction.setCommentId(0);
        userAction.setUserId(videoComment.getUserId());
        userAction.setVideoId(videoComment.getVideoId());
        userAction.setVideoUserId(videoComment.getVideoUserId());
        userAction.setActionTime(videoComment.getPostTime());
        userAction.setActionType(UserActionTypeEnum.VIDEO_COMMENT.getType());
        userAction.setActionCount(1);
        userAction.setVideoUserId(videoInfo.getUserId());
        userActionMapper.insert(userAction);
        videoInfoMapper.updateCountInfo(videoInfo.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(),1);
    }

    @Override
    public VideoComment getCommentByCommentId(Integer commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("评论 ID 不能为空");
        }
        return videoCommentMapper.selectByCommentId(commentId);
    }

    @Override
    public List<VideoComment> findListByParam(VideoCommentQuery query) {
        List<VideoComment> list = new ArrayList<>();
        if(query.getLoadChildren()!=null&&query.getLoadChildren()){
            list = videoCommentMapper.selectListWithChildren(query);
        }else{
            list = videoCommentMapper.selectList(query);
        }
        return list;
    }

    @Override
    public PaginationResult<VideoComment> findListByPage(VideoCommentQuery query) {
        if (query == null) {
            query = new VideoCommentQuery();
        }
        
        // 设置默认分页参数
        if (query.getPageNo() == null || query.getPageNo() < 1) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }
        
        // 创建 SimplePage 对象用于 MyBatis 分页
        SimplePage simplePage = new SimplePage(query.getPageNo(), query.getPageSize());
        query.setSimplePage(simplePage);

        List<VideoComment> list = new ArrayList<>();
        // 查询数据列表
        if(query.getLoadChildren()!=null&&query.getLoadChildren()){
            list = videoCommentMapper.selectListWithChildren(query);
        }else{
            list = videoCommentMapper.selectList(query);
        }
        
        // 查询总数（使用相同的查询条件但不带分页）
        query.setSimplePage(null);
        Integer totalCount = videoCommentMapper.selectCount(query);
        
        // 重新设置分页参数
        query.setSimplePage(simplePage);

        PaginationResult<VideoComment> paginationResult = new PaginationResult();
        paginationResult.setList(list);
        paginationResult.setPageNo(query.getPageNo());
        paginationResult.setPageSize(query.getPageSize());
        paginationResult.setTotalCount(totalCount);
        paginationResult.setTotalPages((totalCount + query.getPageSize() - 1) / query.getPageSize());
        return paginationResult;
    }

    @Override
    public void deleteComment(Integer commentId,String userId) {
        VideoComment dbVideoComment = videoCommentMapper.selectByCommentId(commentId);
        if (dbVideoComment == null) {
            throw new BaseException(600, "评论不存在");
        }
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(dbVideoComment.getVideoId());
        if (videoInfo == null) {
            throw new BaseException(600, "视频不存在");
        }
        if (userId!=null&&!videoInfo.getUserId().equals(userId)&&!dbVideoComment.getUserId().equals(userId)) {
            throw new BaseException(600, "无权限删除该评论");
        }
        videoCommentMapper.deleteByCommentId(commentId);
        Integer count = 1;
        if(dbVideoComment.getPCommentId()==0){
            VideoCommentQuery query = new VideoCommentQuery();
            query.setPCommentId(commentId);
            count +=videoCommentMapper.selectCount(query);
            videoCommentMapper.deleteByParam(query);
        }
        videoInfoMapper.updateCountInfo(videoInfo.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(),-count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topComment(Integer commentId,String userId) {
        this.cancelTopComment(commentId,userId);
        VideoComment videoComment = new VideoComment();
        videoComment.setTopType(1);
        videoComment.setCommentId(commentId);
        videoCommentMapper.updateByCommentId(videoComment);
    }

    @Override
    public void cancelTopComment(Integer commentId,String userId) {
        VideoComment dbVideoComment = videoCommentMapper.selectByCommentId(commentId);
        if (dbVideoComment == null) {
            throw new BaseException(600, "评论不存在");
        }
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(dbVideoComment.getVideoId());
        if(videoInfo == null){
            throw new BaseException(600,"视频不存在");
        }
        if(!videoInfo.getUserId().equals(userId)){
            throw new BaseException(600, "不是视频用户");
        }
        VideoComment videoComment = new VideoComment();
        videoComment.setTopType(0);

        VideoCommentQuery query = new VideoCommentQuery();
        query.setVideoId(dbVideoComment.getVideoId());
        query.setTopType(1);
        videoCommentMapper.updateByParam(videoComment, query);
    }


}
