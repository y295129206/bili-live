package com.bililive.service.impl;

import com.bililive.Enum.SearchOrderTypeEnum;
import com.bililive.Enum.UserActionTypeEnum;
import com.bililive.component.EsSearchComponent;
import com.bililive.mapper.VideoDanmuMapper;
import com.bililive.mapper.VideoInfoMapper;
import com.bililive.pojo.*;
import com.bililive.service.VideoDanmuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频弹幕服务实现类
 */
@Service
@Slf4j
public class VideoDanmuServiceImpl implements VideoDanmuService {

    @Autowired
    private VideoDanmuMapper videoDanmuMapper;

    @Autowired
    private VideoInfoMapper videoInfoMapper;
    @Autowired
    private EsSearchComponent esSearchComponent;
    @Override
    public void saveVideoDanmu(VideoDanmu danmu) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(danmu.getVideoId());
        if(videoInfo == null){
            throw new BaseException(600,"视频不存在");
        }
        if(videoInfo.getInteraction()!=null&&videoInfo.getInteraction().contains("1")){
            throw new BaseException("视频已禁止弹幕");
        }
        videoDanmuMapper.insert(danmu);
        videoDanmuMapper.updateCountInfo(danmu.getVideoId(), UserActionTypeEnum.VIDEO_DANMU.getField(),1);
        esSearchComponent.updateDocCount(danmu.getVideoId(), SearchOrderTypeEnum.DANMU_COUNT.getField(),1);
    }
    @Override
    public List<VideoDanmu> findListByParam(VideoDanmuQuery query) {
        return videoDanmuMapper.selectList(query);
    }

    @Override
    public PaginationResult<VideoDanmu> findListByPage(VideoDanmuQuery query) {
        if (query == null) {
            query = new VideoDanmuQuery();
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
        List<VideoDanmu> list = videoDanmuMapper.selectList(query);
        
        // 查询总数（使用相同的查询条件但不带分页）
        query.setSimplePage(null);
        Integer totalCount = videoDanmuMapper.selectCount(query);
        
        // 重新设置分页参数
        query.setSimplePage(simplePage);
        
        // 计算总页数
        int totalPages = (totalCount + query.getPageSize() - 1) / query.getPageSize();
        
        // 构建分页结果
        PaginationResult<VideoDanmu> paginationResult = new PaginationResult<>();
        paginationResult.setList(list);
        paginationResult.setTotalCount(totalCount);
        paginationResult.setTotalPages(totalPages);
        paginationResult.setPageNo(query.getPageNo());
        paginationResult.setPageSize(query.getPageSize());
        
        return paginationResult;
    }

    @Override
    public void deleteDanmu(Integer danmuId, String userId) {
        VideoDanmu danmu = videoDanmuMapper.selectByDanmuId(danmuId);
        if(danmu == null){
            throw new BaseException(600,"弹幕不存在");
        }
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(danmu.getVideoId());
        if(videoInfo == null){
            throw new BaseException(600,"视频不存在");
        }
        if(userId != null && !videoInfo.getUserId().equals(userId)){
            throw new BaseException(600,"无权限删除");
        }
        videoDanmuMapper.deleteByDanmuId(danmuId);
    }
}
