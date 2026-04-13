package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 简单分页参数类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimplePage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从 1 开始）
     */
    private Integer pageNo = 1;

    /**
     * 每页显示条数
     */
    private Integer pageSize = 10;

    /**
     * 起始索引（用于 SQL LIMIT）
     */
    private Integer start;

    /**
     * 结束索引（用于 SQL LIMIT）
     */
    private Integer end;
    /**
     * 总记录数
     */
    private Integer totalCount;

    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 构造函数：根据页码和每页条数计算起始和结束索引
     */
    public SimplePage(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo != null ? pageNo : 1;
        this.pageSize = pageSize != null ? pageSize : 10;
        if (this.pageNo > 0 && this.pageSize > 0) {
            this.start = (this.pageNo - 1) * this.pageSize;
            this.end = this.pageSize;
        }
    }

    /**
     * 构造函数：根据页码、总数和每页条数计算起始和结束索引
     * @param pageNo 当前页码
     * @param totalCount 总记录数
     * @param pageSize 每页显示条数
     */
    public SimplePage(Integer pageNo, Integer totalCount, Integer pageSize) {
        this.pageNo = pageNo != null ? pageNo : 1;
        this.pageSize = pageSize != null ? pageSize : 10;
        this.totalCount = totalCount;
        if (this.pageNo > 0 && this.pageSize > 0) {
            this.start = (this.pageNo - 1) * this.pageSize;
            this.end = this.pageSize;
        }
    }
}
