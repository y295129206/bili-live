package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysSettingDto {
    private static final long serialVersionUID = -90520876542443938L;
    private Integer registerCoinCount = 10;
    private Integer postVideoCoinCount = 5;
    private Integer VideoSize = 20;  // 单个视频大小限制 20MB
    private Integer VideoPCount = 10;
    private Integer VideoCount = 10;
    private Integer CommentCount = 20;
    private Integer danmuCount = 20;
}
