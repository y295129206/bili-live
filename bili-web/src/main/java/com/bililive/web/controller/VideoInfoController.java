package com.bililive.web.controller;

import com.bililive.Enum.VideoStatusEnum;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.service.VideoInfoPostService;
import com.bililive.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@Slf4j
public class VideoInfoController {
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private VideoInfoPostService videoInfoPostService;

    @RequestMapping("/loadVideoList")
    public Result<PaginationResult> postVideo(Integer status, Integer pageNo, Integer pageSize, String videoNameFuzzy, HttpServletRequest request) {
        log.info("loadVideList:status={},pageNo={},pageSize={},videoNameFuzzy={}",status,pageNo,pageSize,videoNameFuzzy);
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoPostQuery.setPageNo(pageNo != null ? pageNo : 1);
        videoInfoPostQuery.setPageSize(pageSize != null ? pageSize : 10);
        videoInfoPostQuery.setOrderBy("v.create_time desc");
        if(status!=null){
            if(status==-1){
                videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(),VideoStatusEnum.STATUS4.getStatus()});
            }else{
                videoInfoPostQuery.setStatus(status);
            }
        }
        videoInfoPostQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoInfoPostQuery.setQueryCountInfo(true);
        PaginationResult result = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return Result.success(result);
    }
    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
