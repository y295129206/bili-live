package com.bililive.service.impl;

import com.bililive.Enum.SearchOrderTypeEnum;
import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.component.EsSearchComponent;
import com.bililive.mapper.UserActionMapper;
import com.bililive.mapper.UserInfoMapper;
import com.bililive.mapper.VideoCommentMapper;
import com.bililive.mapper.VideoInfoMapper;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.SimplePage;
import com.bililive.pojo.UserAction;
import com.bililive.pojo.UserActionQuery;
import com.bililive.pojo.VideoInfo;
import com.bililive.service.UserActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 用户行为服务实现类
 */
@Service
@Slf4j
public class UserActionServiceImpl implements UserActionService {

    @Autowired
    private UserActionMapper userActionMapper;
    @Autowired
    private VideoInfoMapper videoInfoMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private VideoCommentMapper videoCommentMapper;
    @Autowired
    private EsSearchComponent esSearchComponent;

    @Override
    public void saveAction(UserAction userAction) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(userAction.getVideoId());
        if(videoInfo == null){
            throw new BaseException(600,"视频不存在");
        }
        userAction.setVideoUserId(videoInfo.getUserId());
        UserActionTypeEnum userActionTypeEnum = UserActionTypeEnum.getByType(userAction.getActionType());
        if(userActionTypeEnum==null){
            throw new BaseException(600,"行为类型不存在");
        }
        UserAction dbUserAction = userActionMapper.selectByUserIdAndVideoIdAndActionTypeAndCommentId(
                userAction.getUserId(),userAction.getVideoId(),userAction.getActionType(),userAction.getCommentId());
        userAction.setActionTime(new Date());

        switch (userActionTypeEnum){
            case VIDEO_LIKE:
            case VIDEO_COLLECT:
                if(dbUserAction == null){
                    userActionMapper.insert(userAction);
                }else{
                    userActionMapper.deleteByActionId(dbUserAction.getActionId());
                }
                Integer changeCount = dbUserAction == null ? 1 : -1;
                videoInfoMapper.updateCountInfo(userAction.getVideoId(),userActionTypeEnum.getField(),changeCount);
                if(userActionTypeEnum == UserActionTypeEnum.VIDEO_COLLECT){
                    esSearchComponent.updateDocCount(userAction.getVideoId(), SearchOrderTypeEnum.COLLECT_COUNT.getField(),changeCount);

                }
                break;
            case VIDEO_COIN:
                if(userAction.getUserId().equals(userAction.getVideoUserId())){
                    throw new BaseException(600,"不能给自己投币");
                }
                if(dbUserAction != null){
                    throw new BaseException(600,"不能重复投币");
                }
                Integer changeCount2 = userInfoMapper.updateCoinCountByUserId(userAction.getUserId(),-userAction.getActionCount());
                if(changeCount2 == 0){
                    throw new BaseException(600,"用户余额不足");
                }
                changeCount2 = userInfoMapper.updateCoinCountByUserId(userAction.getVideoUserId(),userAction.getActionCount());
                userActionMapper.insert(userAction);
                videoInfoMapper.updateCountInfo(userAction.getVideoId(),userActionTypeEnum.getField(),userAction.getActionCount());
                break;
            case COMMENT_LIKE:
            case COMMENT_HATE:
                UserActionTypeEnum opposeActionType = UserActionTypeEnum.COMMENT_LIKE==userActionTypeEnum?UserActionTypeEnum.COMMENT_HATE:UserActionTypeEnum.COMMENT_LIKE;
                UserAction opposeAction = userActionMapper.selectByUserIdAndVideoIdAndActionTypeAndCommentId(
                         userAction.getUserId(),userAction.getVideoId(),opposeActionType.getType(),userAction.getCommentId());
                if(opposeAction != null){
                    userActionMapper.deleteByActionId(opposeAction.getActionId());
                }
                if(dbUserAction != null){
                    userActionMapper.deleteByActionId(dbUserAction.getActionId());
                }else{
                    userActionMapper.insert(userAction);
                }
                changeCount = dbUserAction == null ? 1 : -1;
                Integer opposeChangeCount = -changeCount;
                log.info("{},{},{},{},{}",userAction.getCommentId(),userActionTypeEnum.getField(),changeCount,
                        opposeAction==null?null:opposeActionType.getField(),opposeChangeCount);
                videoCommentMapper.updateCountInfo(userAction.getCommentId(),userActionTypeEnum.getField(),changeCount,
                        opposeAction==null?null:opposeActionType.getField(),opposeChangeCount);
                break;
        }
    }

    @Override
    public UserAction getUserActionByParam(String userId, String videoId, Integer actionType) {
        if (userId == null || videoId == null || actionType == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }
        return userActionMapper.selectByUserIdAndVideoIdAndActionType(userId, videoId, actionType);
    }

    @Override
    public UserAction getUserActionByParamWithComment(String userId, String videoId, Integer actionType, Integer commentId) {
        if (userId == null || videoId == null || actionType == null || commentId == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }
        return userActionMapper.selectByUserIdAndVideoIdAndActionTypeAndCommentId(userId, videoId, actionType, commentId);
    }

    @Override
    public List<UserAction> findListByParam(UserActionQuery query) {
        if (query == null) {
            query = new UserActionQuery();
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
        
        return userActionMapper.selectList(query);
    }

    @Override
    public PaginationResult<UserAction> findListByPage(UserActionQuery query) {
        if (query == null) {
            query = new UserActionQuery();
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
        
        // 查询数据列表
        List<UserAction> list = userActionMapper.selectList(query);
        
        // 查询总数（需要清除分页参数）
        query.setSimplePage(null);
        Integer totalCount = userActionMapper.selectCount(query);
        
        // 重新设置分页参数
        query.setSimplePage(simplePage);
        
        // 计算总页数
        int totalPages = (totalCount + query.getPageSize() - 1) / query.getPageSize();

        PaginationResult<UserAction> paginationResult = new PaginationResult<>();
        paginationResult.setList(list);
        paginationResult.setTotalCount(totalCount);
        paginationResult.setTotalPages(totalPages);
        paginationResult.setPageNo(query.getPageNo());
        paginationResult.setPageSize(query.getPageSize());
        
        return paginationResult;
    }
}
