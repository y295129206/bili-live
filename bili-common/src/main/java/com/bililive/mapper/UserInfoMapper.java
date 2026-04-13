package com.bililive.mapper;

import com.bililive.pojo.UserInfo;
import com.bililive.pojo.UserInfoQuery;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserInfoMapper {

    @Select("select * from user_info where email=#{email}")
    UserInfo findByEmail(String email);

    @Select("select * from user_info where nick_name=#{nickName}")
    UserInfo findByNickName(String nickName);

    @Insert("insert into user_info(user_id, nick_name, email, password, theme, status, sex, join_time, total_coin_count, current_coin_count) " +
            "VALUES(#{userId},#{nickName},#{email},#{password},#{theme},#{status},#{sex},#{joinTime},#{totalCoinCount},#{currentCoinCount}) ")
    void insert(UserInfo userInfo);

    Integer updateCoinCountByUserId(@Param("userId") String userId, @Param("changeCount") Integer changeCount);

    void updateByUserId(UserInfo userInfo);

    @Select("select * from user_info where user_id=#{userId}")
    UserInfo findByUserId(String userId);

    /**
     * 根据用户ID列表批量查询用户信息
     * @param userIdList 用户ID列表
     * @return 用户信息列表
     */
    List<UserInfo> selectListByUserIdList(@Param("userIdList") List<String> userIdList);

    /**
     * 根据查询条件查询用户列表
     * @param query 查询条件
     * @return 用户信息列表
     */
    List<UserInfo> selectList(@Param("query") UserInfoQuery query);


    Integer selectCount(@Param("query") UserInfoQuery userInfoQuery);
}
