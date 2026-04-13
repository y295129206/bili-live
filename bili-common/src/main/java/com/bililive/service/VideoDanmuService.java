package com.bililive.service;

import com.bililive.pojo.PaginationResult;
import com.bililive.pojo.VideoDanmu;
import com.bililive.pojo.VideoDanmuQuery;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 视频弹幕服务接口
 */
public interface VideoDanmuService {

    /**
     * 保存弹幕
     * @param danmu 弹幕信息
     */
    void saveVideoDanmu(VideoDanmu danmu);

    /**
     * 根据查询条件获取弹幕列表
     * @param query 查询参数
     * @return 弹幕列表
     */
    List<VideoDanmu> findListByParam(VideoDanmuQuery query);

    /**
     * 根据查询条件获取分页结果
     * @param query 查询参数
     * @return 分页结果
     */
    public PaginationResult<VideoDanmu> findListByPage(VideoDanmuQuery query);

    void deleteDanmu(@NotNull Integer danmuId, String userId);
}
