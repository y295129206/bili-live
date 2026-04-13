package com.bililive.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * 分类信息查询条件封装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryInfoQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码 (默认第1页)
     */
    private Integer pageNum = 1;

    /**
     * 每页显示条数 (默认10条)
     */
    private Integer pageSize = 10;

    /**
     * 分类ID (精确匹配)
     */
    private Integer categoryId;

    /**
     * 分类编码 (支持模糊查询)
     */
    private String categoryCode;

    /**
     * 分类名称 (支持模糊查询)
     */
    private String categoryName;

    /**
     * 父级分类ID (精确匹配，常用于查询某分类下的子分类)
     */
    private Integer pCategoryId;

    /**
     * 排序号 (精确匹配)
     */
    private Integer sort;

    /**
     * 排序字段 (动态排序用，例如 "sort", "category_id")
     */
    private String orderBy;
}