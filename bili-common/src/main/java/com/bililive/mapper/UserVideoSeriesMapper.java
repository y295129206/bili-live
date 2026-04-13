package com.bililive.mapper;

import com.bililive.pojo.UserVideoSeries;
import com.bililive.pojo.UserVideoSeriesQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户视频合集 Mapper 接口
 */
@Mapper
public interface UserVideoSeriesMapper {

    /**
     * 插入视频合集
     * @param userVideoSeries 视频合集对象
     */
    void insert(UserVideoSeries userVideoSeries);

    /**
     * 根据合集 ID 查询视频合集
     * @param seriesId 合集 ID
     * @return 视频合集
     */
    UserVideoSeries selectBySeriesId(@Param("seriesId") Integer seriesId);

    /**
     * 根据用户 ID 查询视频合集列表
     * @param userId 用户 ID
     * @return 视频合集列表
     */
    List<UserVideoSeries> selectListByUserId(@Param("userId") String userId);

    /**
     * 更新视频合集
     * @param userVideoSeries 视频合集对象
     */
    void updateBySeriesId(UserVideoSeries userVideoSeries);

    /**
     * 删除视频合集
     * @param seriesId 合集 ID
     */
    void deleteBySeriesId(@Param("seriesId") Integer seriesId);

    List<UserVideoSeries> selectUserAllSeries(@Param("userId") String userId);

    Integer selectMaxSort(@Param("userId") String userId);

    Integer deleteByParam(UserVideoSeriesQuery query);

    void changeSort(@Param("userVideoSeriesList") List<UserVideoSeries> userVideoSeriesList);

    List<UserVideoSeries> selectListWithVideoList(@Param("query") UserVideoSeriesQuery query);
}
