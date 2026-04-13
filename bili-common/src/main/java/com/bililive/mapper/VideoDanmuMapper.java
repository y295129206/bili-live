package com.bililive.mapper;

import com.bililive.pojo.VideoDanmu;
import com.bililive.pojo.VideoDanmuQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频弹幕 Mapper 接口
 */
@Mapper
public interface VideoDanmuMapper {

    /**
     * 插入弹幕
     * @param danmu 弹幕信息
     * @return 影响行数
     */
    Integer insert(VideoDanmu danmu);

    void updateCountInfo(@Param("videoId")String videoId,@Param("field") String fielId,@Param("changeCount") Integer changeCount);

    /**
     * 根据查询条件获取弹幕列表
     * @param query 查询参数
     * @return 弹幕列表
     */
    List<VideoDanmu> selectList(@Param("query") VideoDanmuQuery query);

    /**
     * 根据查询条件统计弹幕总数
     * @param query 查询参数
     * @return 总数
     */
    Integer selectCount(@Param("query") VideoDanmuQuery query);

    /**
     * 根据弹幕 ID 查询弹幕
     * @param danmuId 弹幕 ID
     * @return 弹幕信息
     */
    VideoDanmu selectByDanmuId(@Param("danmuId") Integer danmuId);

    /**
     * 根据弹幕 ID 删除弹幕
     * @param danmuId 弹幕 ID
     */
    void deleteByDanmuId(@Param("danmuId") Integer danmuId);

    /**
     * 根据视频 ID 删除弹幕
     * @param videoId 视频 ID
     */
    void deleteByVideoId(@Param("videoId") String videoId);
}
