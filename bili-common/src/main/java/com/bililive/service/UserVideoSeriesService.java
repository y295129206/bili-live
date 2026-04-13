package com.bililive.service;

import com.bililive.pojo.UserVideoSeries;
import com.bililive.pojo.UserVideoSeriesQuery;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户视频合集服务接口
 */
public interface UserVideoSeriesService {


    List<UserVideoSeries> getUserAllSeries(@NotEmpty String userId);

    void saveUserVideoSeries(UserVideoSeries userVideoSeries,String VideoIds);

    void saveSeriesVideo(String userId,Integer seriesId,String videoId);

    UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId);

    void delSeriesVideo(String userId, @NotNull Integer seriesId, @NotEmpty String videoId);

    void delVideoSeries(String userId, @NotNull Integer seriesId);

    void changeVideoSeriesSort(String userId, @NotNull String seriesIds);

    List<UserVideoSeries> findListWithVideoList(UserVideoSeriesQuery query);
}
