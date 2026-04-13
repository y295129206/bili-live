package com.bililive.service.impl;

import com.bililive.mapper.VideoPlayHistoryMapper;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.SimplePage;
import com.bililive.pojo.VideoPlayHistory;
import com.bililive.pojo.VideoPlayHistoryQuery;
import com.bililive.service.VideoPlayHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class VideoPlayHistoryServiceImpl implements VideoPlayHistoryService {

    @Autowired
    private VideoPlayHistoryMapper videoPlayHistoryMapper;

    @Override
    public void saveHistory(String userId, String videoId, Integer fileIndex) {
        VideoPlayHistory history = VideoPlayHistory.builder()
                .userId(userId)
                .videoId(videoId)
                .fileIndex(fileIndex)
                .lastUpdateTime(new Date())
                .build();
        videoPlayHistoryMapper.saveOrUpdate(history);
    }

    @Override
    public PaginationResult<VideoPlayHistory> findListByPage(VideoPlayHistoryQuery query) {
        if (query == null) {
            query = new VideoPlayHistoryQuery();
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
        List<VideoPlayHistory> list = videoPlayHistoryMapper.selectList(query);

        // 查询总数（需要清除分页参数）
        query.setSimplePage(null);
        Integer totalCount = videoPlayHistoryMapper.selectCount(query);

        // 重新设置分页参数
        query.setSimplePage(simplePage);

        // 计算总页数
        int totalPages = (totalCount + query.getPageSize() - 1) / query.getPageSize();

        // 构建分页结果
        PaginationResult<VideoPlayHistory> paginationResult = new PaginationResult<>();
        paginationResult.setList(list);
        paginationResult.setTotalCount(totalCount);
        paginationResult.setTotalPages(totalPages);
        paginationResult.setPageNo(query.getPageNo());
        paginationResult.setPageSize(query.getPageSize());

        return paginationResult;
    }

    @Override
    public void deleteByParam(VideoPlayHistoryQuery query) {
        if (query == null) {
            throw new BaseException("查询条件不能为空");
        }
        if (query.getUserId() == null || query.getUserId().isEmpty()) {
            throw new BaseException("用户ID不能为空");
        }
        videoPlayHistoryMapper.deleteByParam(query);
    }
}
