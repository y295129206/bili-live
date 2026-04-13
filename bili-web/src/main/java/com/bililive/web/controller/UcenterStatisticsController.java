package com.bililive.web.controller;

import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.Result;
import com.bililive.pojo.StatisticsInfo;
import com.bililive.pojo.StatisticsInfoQuery;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.service.StatisticsInfoService;
import com.bililive.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ucenter")
@Slf4j
@Validated
public class UcenterStatisticsController {
    @Autowired
    private StatisticsInfoService statisticsInfoService;
    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/getActualTimeStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public Result<Map<String,Object>> getActualTimeStatisticsInfo(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        String preDate = DateUtils.getBeforDayDate2(1);
        StatisticsInfoQuery query = new StatisticsInfoQuery();
        query.setUserId(tokenUserInfoDto.getUserId());
        query.setStatisticsDate(preDate);

        List<StatisticsInfo> preDayData = statisticsInfoService.findListByParam(query);
        Map<Integer, Integer> preDayDataMap = preDayData.stream().collect(Collectors.toMap(StatisticsInfo::getDataType, StatisticsInfo::getStatisticsCount,
                (item1, item2)-> item2));

        Map<String, Integer> totalCountInfo = statisticsInfoService.getStatisticsInfoActualTime(tokenUserInfoDto.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("preDayData",preDayDataMap);
        result.put("totalCountInfo",totalCountInfo);
        return Result.success(result);
    }

    @RequestMapping("/getWeekStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public Result<List<StatisticsInfo>> getWeekStatisticsInfo(@NotNull Integer dataType, HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        List<String> dateList = DateUtils.getBeforDates(7);
        StatisticsInfoQuery query = new StatisticsInfoQuery();
        query.setUserId(tokenUserInfoDto.getUserId());
        query.setDataType(dataType);
        query.setStatisticsDateStart(dateList.get(0));
        query.setStatisticsDateEnd(dateList.get(dateList.size()-1));
        query.setOrderBy("statistics_date asc");
        List<StatisticsInfo> weekData = statisticsInfoService.findListByParam(query);
        Map<String, StatisticsInfo> dataMap = weekData.stream().collect(Collectors.toMap(item->item.getStatisticsDate(),
                Function.identity(),(data1, data2)->data2));
        List<StatisticsInfo> result = new ArrayList<>();
        for (String date : dateList) {
            StatisticsInfo dataItem = dataMap.get(date);
            if (dataItem == null) {
                dataItem = new StatisticsInfo();
                dataItem.setStatisticsDate(date);
                dataItem.setStatisticsCount(0);
            }
            result.add(dataItem);
        }
        return Result.success(result);
    }

    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
