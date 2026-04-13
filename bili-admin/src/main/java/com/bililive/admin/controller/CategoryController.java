package com.bililive.admin.controller;

import com.bililive.pojo.BaseException;
import com.bililive.pojo.CategoryInfo;
import com.bililive.pojo.CategoryInfoQuery;
import com.bililive.pojo.Result;
import com.bililive.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @RequestMapping("/loadCategory")
    public Result loadCategory() {
        // 使用类型转换来访问实现类的特定方法
        if (categoryService instanceof com.bililive.service.impl.CategoryServiceImpl) {
            List<CategoryInfo> treeList = ((com.bililive.service.impl.CategoryServiceImpl) categoryService).getTreeCategoryList();
            return Result.success(treeList);
        } else {
            // 回退到原来的实现
            CategoryInfoQuery query = new CategoryInfoQuery();
            query.setOrderBy("sort asc, category_id asc");
            query.setPageNum(null);
            query.setPageSize(null);
            return Result.success(categoryService.findListByParam(query));
        }
    }

    @RequestMapping("/saveCategory")
    public Result saveCategory(@NotNull Integer pCategoryId,
                               Integer categoryId,
                               @NotEmpty String categoryCode,
                               @NotEmpty String categoryName,
                               String icon,
                               String background,
                               Integer sort
                               ){
        CategoryInfo categoryInfo = CategoryInfo.builder()
                .pCategoryId(pCategoryId)
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .icon(icon)
                .background(background)
                .categoryId(categoryId)
                .sort(sort)
                .build();
        log.info("保存分类信息：{}", categoryInfo);
        categoryService.saveCategory(categoryInfo);
        return Result.success();
    }

    @RequestMapping("/delCategory")
    public Result deleteCategory(@NotNull Integer categoryId) {
        categoryService.deleteCategory(categoryId);
        return Result.success();
    }

    @RequestMapping("/changeSort")
    public Result changeSort(@NotNull Integer pCategoryId,
                                     @NotEmpty String categoryIds) {
        log.info("重新排序 - pCategoryId: {}, categoryIds: {}", pCategoryId, categoryIds);

        // 将逗号分隔的 categoryId 字符串转换为数组
        List<Integer> categoryIdList = Arrays.stream(categoryIds.split(","))
                .map(Integer::parseInt)
                .toList();

        // 如果 pCategoryId 为 null，尝试从第一个 categoryId 推断
        if (pCategoryId == null && !categoryIdList.isEmpty()) {
            CategoryInfo firstCategory = categoryService.getCategoryById(categoryIdList.get(0));
            if (firstCategory != null) {
                pCategoryId = firstCategory.getPCategoryId();
                log.info("从第一个分类推断 pCategoryId: {}", pCategoryId);
            }
        }

        if (pCategoryId == null || !StringUtils.hasText(categoryIds)) {
            throw new BaseException("参数错误：pCategoryId=" + pCategoryId + ", categoryIds=" + categoryIds);
        }

        categoryService.changeCategorySort(pCategoryId, categoryIdList);
        return Result.success();
    }

}
