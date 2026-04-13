package com.bililive.web.task;

import com.bililive.service.StatisticsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SysTask {
    @Autowired
    private StatisticsInfoService statisticsInfoService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void statisticsData(){
        statisticsInfoService.statisticsData();
    }
}
