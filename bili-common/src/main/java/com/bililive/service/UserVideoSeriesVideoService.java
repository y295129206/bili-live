package com.bililive.service;

import com.bililive.pojo.UserVideoSeriesVideo;
import com.bililive.pojo.UserVideoSeriesVideoQuery;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户视频合集-视频关联服务接口
 */
public interface UserVideoSeriesVideoService {


    /**
     * 根据查询条件获取用户行为列表
     * @param query 查询参数
     * @return 用户行为列表
     */
    List<UserVideoSeriesVideo> findListByParam(UserVideoSeriesVideoQuery query);
}
