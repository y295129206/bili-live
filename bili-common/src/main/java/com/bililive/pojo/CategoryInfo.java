package com.bililive.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分类信息实体类
 * 对应数据库表：category_info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer categoryId;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 父级分类ID
     */
    private Integer pCategoryId;

    /**
     * 图标
     */
    private String icon;

    /**
     * 背景图
     */
    private String background;

    /**
     * 排序号
     */
    private Integer sort;
    
    /**
     * 子分类列表（用于前端树形展示）
     */
    private List<CategoryInfo> children;
}