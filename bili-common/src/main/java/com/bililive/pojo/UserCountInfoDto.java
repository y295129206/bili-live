package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCountInfoDto {
    private Integer fansCount;
    private Integer focusCount;
    private Integer currentCoinCount;
    private Integer playCount;
    private Integer likeCount;
}
