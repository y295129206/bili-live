package com.bililive.mapper;

import com.bililive.pojo.UserFocus;
import com.bililive.pojo.UserFocusQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户关注 Mapper 接口
 */
@Mapper
public interface UserFocusMapper {

    /**
     * 插入关注记录
     * @param userFocus 关注对象
     */
    void insert(UserFocus userFocus);

    /**
     * 根据用户 ID 和关注用户 ID 查询关注
     * @param userId 用户 ID
     * @param focusUserId 关注用户 ID
     * @return 关注记录
     */
    UserFocus selectByUserIdAndFocusUserId(@Param("userId") String userId, 
                                           @Param("focusUserId") String focusUserId);


    /**
     * 根据查询条件统计关注总数
     * @return 总数
     */
    Integer selectFansCount(String userId);

    /**
     * 根据查询条件统计关注总数
     * @return 总数
     */
    Integer selectFocusCount(String userId);

    /**
     * 删除关注
     * @param userId 用户 ID
     * @param focusUserId 关注用户 ID
     */
    void deleteByUserIdAndFocusUserId(@Param("userId") String userId, 
                                      @Param("focusUserId") String focusUserId);

    /**
     * 根据查询条件查询关注列表
     * @param query 查询条件
     * @return 关注列表
     */
    List<UserFocus> selectList(@Param("query") UserFocusQuery query);

    /**
     * 根据查询条件统计关注总数
     * @param query 查询条件
     * @return 总数
     */
    Integer selectCount(@Param("query") UserFocusQuery query);

}
