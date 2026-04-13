package com.bililive.component;

import com.bililive.Enum.DateTimePatternEnum;
import com.bililive.entity.config.AppConfig;
import com.bililive.entity.constants.Constants;
import com.bililive.pojo.*;
import com.bililive.redis.RedisUtils;
import com.bililive.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.*;

@Component
@Slf4j
public class RedisComponent {
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private AppConfig appConfig;

    public String saveCheckCode(String code){
        String checkCodeKey = UUID.randomUUID().toString();
        //创建redis，唯一名称。存储对象。持续时间
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey,code,Constants.REDIS_KEY_EXPIRES_ONE_MIN * 10);
        return checkCodeKey;
    }

    public String getCheckCode(String code){
        return (String)redisUtils.get(Constants.REDIS_KEY_CHECK_CODE+code);
    }

    public void cleanCheckCode(String code) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+code);
    }

    public void saveToken(TokenUserInfoDto tokenUserInfoDto){
        String token = UUID.randomUUID().toString();
        tokenUserInfoDto.setExpireAt(System.currentTimeMillis()+Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
        tokenUserInfoDto.setToken(token);
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB+token,tokenUserInfoDto,Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
    }

    public void cleanToken(String token){
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB+token);
    }

    public TokenUserInfoDto getTokenInfo(String token){
        return (TokenUserInfoDto)redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB+token);
    }
    public String getTokenInfoforAdmin(String token){
        return (String) redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN+token);
    }
    public String saveTokenforAdmin(String account){
        String token = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN+token,account,Constants.REDIS_KEY_EXPIRES_ONE_DAY);
        return token;
    }

    public void cleanTokenforAdmin(String token){
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN+token);
    }

    public String savePreview(String userId,String fileName,Integer chunks){
        String uploadId = UUID.randomUUID().toString();
        String day = DateUtils.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        String filePath = day+"/"+userId+uploadId;
        String folder = appConfig.getProjectFolder()+Constants.FILE_FOLDER+Constants.FILE_FOLDER_TEMP+filePath;
        File folderFile = new File(folder);
        if(!folderFile.exists()){
            folderFile.mkdirs();
        }
        UploadingFileDto fileDto = UploadingFileDto.builder()
                .uploadId(uploadId)
                .fileName(fileName)
                .chunks(chunks)
                .chunkIndex(0)
                .fileSize(0L)
                .filePath(filePath)
                .build();
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE+userId+uploadId,fileDto,Constants.REDIS_KEY_EXPIRES_ONE_DAY);
        return uploadId;
    }

    public UploadingFileDto getPreview(String userId, @NotNull String uploadId) {
        return (UploadingFileDto) redisUtils.get(Constants.REDIS_KEY_UPLOADING_FILE+userId+uploadId);
    }

    public SysSettingDto getSysSettingDto(){
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if(sysSettingDto==null){
            sysSettingDto = new SysSettingDto();
        }
        return sysSettingDto;
    }
    public void updatePreview(String userId, UploadingFileDto fileDto){
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE+userId+fileDto.getUploadId(),fileDto,Constants.REDIS_KEY_EXPIRES_ONE_DAY);
    }
    public void delPreview(String userId, String uploadId){
        redisUtils.delete(Constants.REDIS_KEY_UPLOADING_FILE+userId+uploadId);
    }

    public void addFile2DelQueue(String videoId, List<String> deleteFileIndexList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_FILE_DEL+videoId,deleteFileIndexList,Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
    }

    public UploadingFileDto getUploadingVideoFile(String userId, String uploadId){
        return (UploadingFileDto) redisUtils.get(Constants.REDIS_KEY_UPLOADING_FILE+userId+uploadId);
    }
    public void addFile2TransferQueue(List<VideoInfoFilePost> addFileList) {
        log.info("准备将 {} 个文件加入转码队列", addFileList.size());
        for(VideoInfoFilePost file : addFileList){
            log.info("加入转码队列的文件：uploadId={}, fileId={}, fileName={}", file.getUploadId(), file.getFileId(), file.getFileName());
        }
        redisUtils.lpushAll(Constants.REDIS_KEY_QUEUE_TRANSFER,addFileList,0);
        log.info("成功将 {} 个文件加入转码队列", addFileList.size());
    }
    public void addFile2TransferQueue4singal(VideoInfoFilePost videoInfoFile){
        redisUtils.lpush(Constants.REDIS_KEY_QUEUE_TRANSFER,videoInfoFile,0L);
    }

    public VideoInfoFilePost getFileFromTransferQueue() {
        return(VideoInfoFilePost) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_TRANSFER);
    }

    public List<String> getDelFileList(String videoId){
        return redisUtils.getQueueList(Constants.REDIS_KEY_FILE_DEL+videoId);
    }

    public void cleanDelFileList(String videoId) {
        redisUtils.delete(Constants.REDIS_KEY_FILE_DEL+videoId);
    }
    public Integer reportVideoPlayOnline(String fileId,String deviceId){
        String userPlayOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER,fileId,deviceId);
        String playOnlineCountKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE,fileId);
        if(!redisUtils.keyExists(userPlayOnlineKey)){
            redisUtils.setex(userPlayOnlineKey,fileId,Constants.REDIS_KEY_EXPIRES_ONE_SECONDS*8);
            return redisUtils.incrementex(playOnlineCountKey,Constants.REDIS_KEY_EXPIRES_ONE_SECONDS*10).intValue();
        }
        redisUtils.expire(playOnlineCountKey,Constants.REDIS_KEY_EXPIRES_ONE_SECONDS*10);
        redisUtils.expire(userPlayOnlineKey,Constants.REDIS_KEY_EXPIRES_ONE_SECONDS*8);
        Integer count = (Integer) redisUtils.get(playOnlineCountKey);
        return count == null ? 1 : count;
    }

    public Integer decrementPlayOnlineCount(String playOnlineCountKey){
        Long count = redisUtils.decrement(playOnlineCountKey);
        return count == null ? 0 : count.intValue();
    }

    public void updateToken(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB+tokenUserInfoDto.getToken(),tokenUserInfoDto,Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
    }

    public void addKeywordCount(String keyWord){
        redisUtils.zaddCount(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT,keyWord);
    }

    public List<String> getKeywordTop(Integer top){
        return redisUtils.getZSetList(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT,top-1);
    }

    public void addVideoPlay(VideoPlayInfo videoPlayInfo) {
        redisUtils.lpush(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY,videoPlayInfo,null);
    }
    public VideoPlayInfo getVideoPlayFromVideoPlayQueue() {
        return(VideoPlayInfo) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY);
    }

    public void recordVideoPlayCount(String videoId) {
        String date = DateUtils.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        redisUtils.incrementex(Constants.REDIS_KEY_VIDEO_PLAY_COUNT+date+":"+videoId,Constants.REDIS_KEY_EXPIRES_ONE_DAY*2L);
    }

    public Map<String, Integer> getVideoPlayCount(String date) {
        Map<String, Integer> videoPlayCount = redisUtils.getBatch(Constants.REDIS_KEY_VIDEO_PLAY_COUNT+date);
        return videoPlayCount == null ? new HashMap<>() : videoPlayCount;
    }

    public void saveSettingDto(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingDto);
    }
}
