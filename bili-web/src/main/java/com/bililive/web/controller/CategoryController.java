package com.bililive.web.controller;

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

    @RequestMapping("/loadAllCategory")
    public Result loadAllCategory() {
            CategoryInfoQuery query = new CategoryInfoQuery();
            query.setOrderBy("sort asc, category_id asc");
            query.setPageNum(null);
            query.setPageSize(null);
            return Result.success(categoryService.findListByParam(query));
        }

}
