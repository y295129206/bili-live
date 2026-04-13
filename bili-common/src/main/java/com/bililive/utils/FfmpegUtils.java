package com.bililive.utils;

import com.bililive.entity.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bililive.entity.constants.Constants;

import java.io.File;
import java.math.BigDecimal;

/**
 * FFmpeg 工具类
 * 用于视频转码、截图、获取视频信息等操作
 */
@Component
@Slf4j
public class FfmpegUtils {

    @Autowired
    private AppConfig appConfig;
    public void transcode(String filePath){
        String CMD = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";
        CMD = String.format(CMD,filePath,filePath+Constants.IMAGE_THUMBNAIL_SUFFIX);
        ProcessUtils.executeCommand(CMD,appConfig.getShowFFmpegLog());
    }
    public Integer getVideoInfoDuration(String completeVideo){
        final String CMD_GET_CODE = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"";
        String CMD = String.format(CMD_GET_CODE,completeVideo);
        String result = ProcessUtils.executeCommand(CMD,appConfig.getShowFFmpegLog());
        if(result == null){
            return 0;
        }
        result = result.replace("\n","");
        return new BigDecimal( result).intValue();
    }

    public String getVideoCodec(String videoFilePath){
        final String CMD_GET_CODE = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%s\"";
        String CMD = String.format(CMD_GET_CODE,videoFilePath);
        String result = ProcessUtils.executeCommand(CMD,appConfig.getShowFFmpegLog());
        result = result.replace("\n","");
        result = result.substring(result.indexOf("=")+1);
        String codec = result.substring(0,result.indexOf("["));
        return codec;
    }

    public void convertHevc2Mp4(String tempFilePath,String videoFilePath){
        String CMD_HEVC_264 = "ffmpeg -i \"%s\" -c:v libx264 -crf 20 \"%s\" -y" ;
        String cmd = String.format(CMD_HEVC_264,tempFilePath,videoFilePath);
        ProcessUtils.executeCommand(cmd,appConfig.getShowFFmpegLog());
    }

    public void convertVideo2Ts(File tsFolder, String videoFilePath){
        final String CMD_TRANSFER_2TS = "ffmpeg -i \"%s\" -vcodec copy -acodec copy -vbsf h264_mp4toannexb \"%s\" -y";
        // 使用 HLS 格式生成 m3u8 和 ts 切片
        final String CMD_CUT_TS = "ffmpeg -i \"%s\" -c copy -map 0 -f segment -segment_list \"%s\" -segment_time 10 \"%s/%%4d.ts\" -y";
        String tsPath = tsFolder.getAbsolutePath() + File.separator + Constants.TS_NAME;
        String m3u8Path = tsFolder.getAbsolutePath() + File.separator + "index.m3u8";
        //生成 ts 文件
        String cmd = String.format(CMD_TRANSFER_2TS,videoFilePath,tsPath);
        ProcessUtils.executeCommand(cmd,appConfig.getShowFFmpegLog());
        //生成索引文件 m3u8 和切片 ts
        cmd = String.format(CMD_CUT_TS,tsPath,m3u8Path,tsFolder.getAbsolutePath());
        ProcessUtils.executeCommand(cmd,appConfig.getShowFFmpegLog());
        //删除 ts 文件
        new File(tsPath).delete();
    }
}
