package com.bililive.admin.controller;

import com.bililive.Enum.StatisticsTypeEnum;
import com.bililive.pojo.Result;
import com.bililive.pojo.StatisticsInfo;
import com.bililive.pojo.StatisticsInfoQuery;
import com.bililive.pojo.TokenUserInfoDto;
import com.bililive.service.StatisticsInfoService;
import com.bililive.service.UserInfoService;
import com.bililive.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Validated
@RequestMapping("/index")
public class IndexController {
    @Autowired
    private StatisticsInfoService statisticsInfoService;
    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/getActualTimeStatisticsInfo")
    public Result<Map<String,Object>> getActualTimeStatisticsInfo(){
        String preDate = DateUtils.getBeforDayDate2(1);
        StatisticsInfoQuery query = new StatisticsInfoQuery();
        query.setStatisticsDate(preDate);
        List<StatisticsInfo> preDayData = statisticsInfoService.findListByParam(query);
        Map<Integer, Integer> preDayDataMap = preDayData.stream().collect(Collectors.toMap(StatisticsInfo::getDataType, StatisticsInfo::getStatisticsCount,
                (item1, item2)-> item2));
        preDayDataMap.remove(StatisticsTypeEnum.FANS.getType());
        preDayDataMap.put(StatisticsTypeEnum.FANS.getType(),userInfoService.getPreDayUserCount(preDate));
        Map<String, Integer> totalCountInfo = statisticsInfoService.getStatisticsInfoActualTime(null);
        Map<String, Object> result = new HashMap<>();
        result.put("preDayData",preDayDataMap);
        result.put("totalCountInfo",totalCountInfo);
        return Result.success(result);
    }

    @RequestMapping("/getWeekStatisticsInfo")
    public Result<List<StatisticsInfo>> getWeekStatisticsInfo(@NotNull Integer dataType){
        List<String> dateList = DateUtils.getBeforDates(7);
        StatisticsInfoQuery query = new StatisticsInfoQuery();
        query.setDataType(dataType);
        query.setStatisticsDateStart(dateList.get(0));
        query.setStatisticsDateEnd(dateList.get(dateList.size()-1));
        query.setOrderBy("statistics_date asc");
        List<StatisticsInfo> weekData = null;
        if(!StatisticsTypeEnum.FANS.getType().equals(dataType)){
            weekData = statisticsInfoService.findListByParam(query);
        }else{
            weekData = statisticsInfoService.findUserCountTotalByParam(query);
        }
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
}
