package com.bililive.mapper;

import com.bililive.pojo.CategoryInfo;
import com.bililive.pojo.CategoryInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("select * from category_info where category_code = #{categoryCode}")
    CategoryInfo selectByCategoryCode(String categoryCode);
    
    /**
     * 根据分类ID查询分类信息
     */
    @Select("select * from category_info where category_id = #{categoryId}")
    CategoryInfo selectByCategoryId(Integer categoryId);
    
    /**
     * 插入新的分类信息
     */
    void insert(CategoryInfo categoryInfo);
    
    /**
     * 更新分类信息
     */
    void updateByCategoryId(CategoryInfo categoryInfo);
    
    /**
     * 删除分类信息
     */
    void deleteByCategoryId(Integer categoryId);
    
    /**
     * 根据查询条件获取分类列表
     */
    List<CategoryInfo> selectList(CategoryInfoQuery query);
    
    /**
     * 获取所有一级分类（树形结构）
     */
    List<CategoryInfo> selectTopLevelCategories();
    
    /**
     * 查询子分类
     */
    List<CategoryInfo> selectChildren(Integer categoryId);
    
    /**
     * 获取分类总数
     */
    Integer selectCount(CategoryInfoQuery query);
    
    /**
     * 根据父分类ID获取子分类列表
     */
    List<CategoryInfo> selectByParentId(Integer pCategoryId);
}
