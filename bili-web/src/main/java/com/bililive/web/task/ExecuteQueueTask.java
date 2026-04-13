package com.bililive.web.task;

import com.bililive.Enum.SearchOrderTypeEnum;
import com.bililive.component.EsSearchComponent;
import com.bililive.component.RedisComponent;
import com.bililive.pojo.VideoInfoFilePost;
import com.bililive.pojo.VideoPlayHistory;
import com.bililive.pojo.VideoPlayInfo;
import com.bililive.service.VideoInfoFilePostService;
import com.bililive.service.VideoInfoPostService;
import com.bililive.service.VideoInfoService;
import com.bililive.service.VideoPlayHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class ExecuteQueueTask {
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private VideoInfoPostService videoInfoPostService;
    @Autowired
    private VideoInfoService videoInfoService;
    @Autowired
    private EsSearchComponent esSearchComponent;
    @Autowired
    private VideoPlayHistoryService videoPlayHistoryService;

    @PostConstruct
    public void consumTrasnferFileQueue(){
        executorService.execute(()-> {
            while(true){
                VideoInfoFilePost videoInfoFile = null;
                try {
                    videoInfoFile = redisComponent.getFileFromTransferQueue();
                    if(videoInfoFile==null){
                        Thread.sleep(1500);
                        continue;
                    }
                    log.info("开始执行文件上传任务：{}",videoInfoFile);
                    videoInfoPostService.transferVideoFile(videoInfoFile);
                } catch (Exception e) {
                    log.error("执行队列任务异常",e);
                }finally {
                }
            }
        });
    }

    @PostConstruct
    public void consumVideoPlayQueue(){
        executorService.execute(()-> {
            while(true){
                VideoPlayInfo videoPlayInfo = null;
                try {
                    videoPlayInfo = redisComponent.getVideoPlayFromVideoPlayQueue();
                    if(videoPlayInfo==null){
                        Thread.sleep(1500);
                        continue;
                    }
                    videoInfoService.addReadCount(videoPlayInfo.getVideoId());
                    if(videoPlayInfo.getUserId()!=null){
                        videoPlayHistoryService.saveHistory(videoPlayInfo.getUserId(),videoPlayInfo.getVideoId(),videoPlayInfo.getFileIndex());
                    }
                    redisComponent.recordVideoPlayCount(videoPlayInfo.getVideoId());
                    esSearchComponent.updateDocCount(videoPlayInfo.getVideoId(), SearchOrderTypeEnum.PLAY_COUNT.getField(),1);

                } catch (Exception e) {
                    log.error("获取视频播放文件消息队列信息失败",e);
                }finally {
                }
            }
        });
    }

}
