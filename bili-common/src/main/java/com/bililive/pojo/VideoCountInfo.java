package com.bililive.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 视频统计信息类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCountInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核通过数量
     */
    private Integer auditPassCount;

    /**
     * 审核未通过数量
     */
    private Integer auditFailCount;

    /**
     * 待审核数量
     */
    private Integer inProgress;
}
