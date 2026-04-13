package com.bililive.service.impl;

import com.bililive.mapper.CategoryMapper;
import com.bililive.pojo.*;
import com.bililive.service.CategoryService;
import com.bililive.service.VideoInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private VideoInfoService videoInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCategory(CategoryInfo categoryInfo) {
        // 参数校验
        if (categoryInfo == null) {
            throw new BaseException("分类信息不能为空");
        }
        
        if (!StringUtils.hasText(categoryInfo.getCategoryCode())) {
            throw new BaseException("分类编码不能为空");
        }
        
        if (!StringUtils.hasText(categoryInfo.getCategoryName())) {
            throw new BaseException("分类名称不能为空");
        }
        
        // 处理一级分类的pCategoryId
        if (categoryInfo.getPCategoryId() == null || categoryInfo.getPCategoryId() == 0) {
            categoryInfo.setPCategoryId(0); // 一级分类设置为0
        }
        
        // 检查分类编码是否已存在
        CategoryInfo dbBean = this.categoryMapper.selectByCategoryCode(categoryInfo.getCategoryCode());
        
        // 如果是新增操作
        if (categoryInfo.getCategoryId() == null) {
            if (dbBean != null) {
                throw new BaseException("分类编码已存在");
            }
            // 设置默认排序值
            if (categoryInfo.getSort() == null) {
                categoryInfo.setSort(0);
            }
            // 执行插入操作
            categoryMapper.insert(categoryInfo);
        } else {
            // 如果是更新操作
            if (dbBean != null && !dbBean.getCategoryId().equals(categoryInfo.getCategoryId())) {
                throw new BaseException("分类编码已存在");
            }
            // 检查要更新的分类是否存在
            CategoryInfo existCategory = categoryMapper.selectByCategoryId(categoryInfo.getCategoryId());
            if (existCategory == null) {
                throw new BaseException("要更新的分类不存在");
            }
            // 执行更新操作
            categoryMapper.updateByCategoryId(categoryInfo);
        }
    }

    @Override
    public List<CategoryInfo> findListByParam(CategoryInfoQuery query) {
        if (query == null) {
            query = new CategoryInfoQuery();
        }
        
        // 设置默认分页参数
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }
        
        // 如果没有指定排序，默认按sort升序排列
        if (query.getOrderBy() == null) {
            query.setOrderBy("sort asc, category_id asc");
        }
        
        return categoryMapper.selectList(query);
    }
    
    /**
     * 获取树形结构的分类列表（用于前端展示）
     */
    public List<CategoryInfo> getTreeCategoryList() {
        return categoryMapper.selectTopLevelCategories();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new BaseException("分类ID不能为空");
        }
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setCategoryIdOrPCategoryId(categoryId);
        Integer count = videoInfoService.findCountByParam(videoInfoQuery);
        if(count > 0){
            throw new BaseException("该分类下存在视频，请先删除视频");
        }
        CategoryInfo categoryInfo = categoryMapper.selectByCategoryId(categoryId);
        if (categoryInfo == null) {
            throw new BaseException("要删除的分类不存在");
        }
        
        // 检查是否有子分类
        List<CategoryInfo> children = categoryMapper.selectChildren(categoryId);
        if (children != null && !children.isEmpty()) {
            throw new BaseException("该分类下存在子分类，无法删除");
        }
        
        categoryMapper.deleteByCategoryId(categoryId);
    }
    
    @Override
    public CategoryInfo getCategoryById(Integer categoryId) {
        if (categoryId == null) {
            throw new BaseException("分类ID不能为空");
        }
        return categoryMapper.selectByCategoryId(categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeCategorySort(Integer pCategoryId, List<Integer> categoryIds) {
        if (pCategoryId == null || categoryIds == null || categoryIds.isEmpty()) {
            throw new BaseException("参数错误");
        }
        
        // 获取该父分类下的所有子分类
        List<CategoryInfo> categories = categoryMapper.selectByParentId(pCategoryId);
        if (categories == null || categories.isEmpty()) {
            throw new BaseException("未找到相关分类");
        }
        
        // 按照传入的 categoryId 顺序重新设置 sort 值
        for (int i = 0; i < categoryIds.size(); i++) {
            Integer categoryId = categoryIds.get(i);
            // 查找对应的分类
            CategoryInfo category = categories.stream()
                    .filter(c -> c.getCategoryId().equals(categoryId))
                    .findFirst()
                    .orElseThrow(() -> new BaseException("分类不存在：" + categoryId));
            
            // 更新排序值（从 0 开始）
            category.setSort(i);
            categoryMapper.updateByCategoryId(category);
        }
    }

}
