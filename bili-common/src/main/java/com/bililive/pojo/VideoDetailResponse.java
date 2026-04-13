package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 视频详情响应（包含视频信息和用户互动状态）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频信息
     */
    private VideoInfo videoInfo;

    /**
     * 用户互动信息
     */
    private List<UserAction> userActionList;

}
