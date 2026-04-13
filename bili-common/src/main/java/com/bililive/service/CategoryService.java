package com.bililive.service;

import com.bililive.pojo.CategoryInfo;
import com.bililive.pojo.CategoryInfoQuery;

import java.util.List;

public interface CategoryService {
    void saveCategory(CategoryInfo categoryInfo);
    public List<CategoryInfo> findListByParam(CategoryInfoQuery query);
    
    // 添加删除分类方法
    void deleteCategory(Integer categoryId);
    
    // 添加根据 ID 获取分类详情方法
    CategoryInfo getCategoryById(Integer categoryId);

    // 添加修改排序方法
    void changeCategorySort(Integer pCategoryId, List<Integer> categoryIds);

}
