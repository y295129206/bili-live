package com.bililive.service.impl;

import com.bililive.Enum.VideoFileTransferResultEnum;
import com.bililive.Enum.VideoFileUpdateTypeEnum;
import com.bililive.Enum.VideoStatusEnum;
import com.bililive.component.EsSearchComponent;
import com.bililive.component.RedisComponent;
import com.bililive.entity.config.AppConfig;
import com.bililive.entity.constants.Constants;
import com.bililive.mapper.*;
import com.bililive.pojo.*;
import com.bililive.service.VideoInfoPostService;
import com.bililive.utils.CopyTools;
import com.bililive.utils.FfmpegUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 视频信息服务实现类
 */
@Service
@Slf4j
public class VideoInfoPostServiceImpl implements VideoInfoPostService {

    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private VideoInfoPostMapper videoInfoPostMapper;
    @Autowired
    private VideoInfoFilePostMapper videoInfoFilePostMapper;
    @Autowired
    private VideoInfoMapper videoInfoMapper;
    @Autowired
    private VideoInfoFileMapper videoInfoFileMapper;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private FfmpegUtils ffmpegUtils;
    @Autowired
    private EsSearchComponent esSearchComponent;
    @Autowired
    private UserInfoMapper userInfoMapper;

    public VideoInfoPostServiceImpl(RedisComponent redisComponent) {
        this.redisComponent = redisComponent;
    }

    @Override
    @Transactional
    public void saveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> uploadFileList) {
        // 确保 userId 不为空
        if(videoInfoPost.getUserId() == null || videoInfoPost.getUserId().isEmpty()) {
            throw new BaseException(600, "用户 ID 不能为空");
        }
        
        if(uploadFileList.size()>redisComponent.getSysSettingDto().getVideoPCount()){
            throw new BaseException(600,"视频分片数量不能超过"+redisComponent.getSysSettingDto().getVideoPCount());
        }
        if(videoInfoPost.getVideoId()!=null){
            VideoInfoPost videoInfoPostDb = videoInfoPostMapper.selectByVideoId(videoInfoPost.getVideoId());
            if(videoInfoPostDb==null){
                throw new BaseException(600,"视频不存在");
            }
            if(ArrayUtils.contains(new Integer[]{VideoStatusEnum.STATUSO.getStatus(),VideoStatusEnum.STATUS2.getStatus()},videoInfoPostDb.getStatus())){
                throw new BaseException(600,"视频正在转码中或待审核中，请勿重复提交");
            }
        }
        Date curDate = new Date();
        String videoId = videoInfoPost.getVideoId();
        List<VideoInfoFilePost> deleteFileList = new ArrayList<>();
        List<VideoInfoFilePost> addFileList = uploadFileList;
        if(videoId== null){
            videoId = UUID.randomUUID().toString();
            videoInfoPost.setVideoId(videoId);
            videoInfoPost.setCreateTime(curDate);
            videoInfoPost.setLastUpdateTime(curDate);
            videoInfoPost.setStatus(VideoStatusEnum.STATUSO.getStatus());
            videoInfoPostMapper.insert(videoInfoPost);
        }else{
            VideoInfoFilePostQuery fileQuery = new VideoInfoFilePostQuery();
            fileQuery.setVideoId(videoId);
            fileQuery.setUserId(videoInfoPost.getUserId());
            List<VideoInfoFilePost> dbInfoFileList = videoInfoFilePostMapper.selectList(fileQuery);
            Map<String,VideoInfoFilePost> uploadFileMap = uploadFileList.stream().collect(Collectors.toMap(item->item.getUploadId(), Function.identity(),
                    (data1, data2)->data2));
            Boolean updateFileName = false;
            for(VideoInfoFilePost dbInfoFile:dbInfoFileList){
                VideoInfoFilePost uploadFile = uploadFileMap.get(dbInfoFile.getUploadId());
                if(uploadFile==null){
                    deleteFileList.add(dbInfoFile);
                }else if(!uploadFile.getFileName().equals(dbInfoFile.getFileName())){
                    updateFileName = true;
                }
            }
            addFileList = uploadFileList.stream().filter(item->item.getFileId()==null).collect(Collectors.toList());
            videoInfoPost.setLastUpdateTime(curDate);

            Boolean changeVideoInfo = this.changeVideoInfo(videoInfoPost);
            if(addFileList!=null&&!addFileList.isEmpty()){
                videoInfoPost.setStatus(VideoStatusEnum.STATUSO.getStatus());
            }else if(changeVideoInfo||updateFileName){
                videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
            }
            videoInfoPostMapper.updateByVideoId(videoInfoPost,videoInfoPost.getVideoId());
        }
        if(!deleteFileList.isEmpty()){
            List<String> deleteFileIdList = deleteFileList.stream().map(item->item.getFileId()).collect(Collectors.toList());
            videoInfoFilePostMapper.deleteBatchByFileId(deleteFileIdList,videoInfoPost.getUserId());

            List<String> deleteFilePathList = deleteFileList.stream().map(item->item.getFilePath()).collect(Collectors.toList());
            redisComponent.addFile2DelQueue(videoInfoPost.getVideoId(),deleteFilePathList);
        }

        Integer index = 1;
        for(VideoInfoFilePost videoInfoFile:uploadFileList){
            videoInfoFile.setFileIndex(index++);
            videoInfoFile.setVideoId(videoId);
            videoInfoFile.setUserId(videoInfoPost.getUserId());
            if(videoInfoFile.getFileId()==null){
                videoInfoFile.setFileId(UUID.randomUUID().toString());
                videoInfoFile.setUpdateType(VideoFileUpdateTypeEnum.UPDATE.getStatus());
                videoInfoFile.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
            }else{
                // 已存在的文件，保持原有的 transfer_result 和 update_type
                if(videoInfoFile.getTransferResult() == null){
                    videoInfoFile.setTransferResult(VideoFileTransferResultEnum.SUCCESS.getStatus());
                }
                if(videoInfoFile.getUpdateType() == null){
                    videoInfoFile.setUpdateType(VideoFileUpdateTypeEnum.NO_UPDATE.getStatus());
                }
            }
        }
        videoInfoFilePostMapper.insertOrUpdateBatch(uploadFileList);
        if(addFileList!=null&&!addFileList.isEmpty()){
            for(VideoInfoFilePost addFile:addFileList){
                addFile.setUserId(videoInfoPost.getUserId());
                addFile.setVideoId(videoId);
            }
            redisComponent.addFile2TransferQueue(addFileList);
        }
    }

    @Override
    public void transferVideoFile(VideoInfoFilePost videoInfoFilePost) {
        VideoInfoFilePost updateFilePost = new VideoInfoFilePost();
        try {
            UploadingFileDto fileDto = redisComponent.getUploadingVideoFile(videoInfoFilePost.getUserId(),videoInfoFilePost.getUploadId());
            // 检查 Redis 中是否存在上传文件信息
            if (fileDto == null) {
                log.error("Redis 中未找到上传文件信息，userId: {}, uploadId: {}", 
                    videoInfoFilePost.getUserId(), videoInfoFilePost.getUploadId());
                updateFilePost.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
                videoInfoFilePostMapper.updateByUploadIdAndUserId(updateFilePost,videoInfoFilePost.getUploadId(),videoInfoFilePost.getUserId());
                return;
            }
            
            String tempFilePath = appConfig.getProjectFolder()+ Constants.FILE_FOLDER+Constants.FILE_FOLDER_TEMP+fileDto.getFilePath();
            File tempFile = new File(tempFilePath);

            String targetFilePath = appConfig.getProjectFolder()+ Constants.FILE_FOLDER+Constants.FILE_VIDEO+fileDto.getFilePath();
            File targetFile = new File(targetFilePath);

            FileUtils.copyDirectory(tempFile,targetFile);
            //删除临时文件
            FileUtils.forceDelete(tempFile);
            redisComponent.delPreview(videoInfoFilePost.getUserId(),videoInfoFilePost.getUploadId());
            //合并文件
            String completeVideo = targetFilePath+Constants.TEMP_VIDEO_NAME;
            this.union(targetFilePath,completeVideo,true);
            //获取视频时长
            Integer duration = ffmpegUtils.getVideoInfoDuration(completeVideo);
            updateFilePost.setDuration(duration);
            updateFilePost.setFilePath(Constants.FILE_VIDEO+fileDto.getFilePath());
            updateFilePost.setFileSize(new File(completeVideo).length());
            updateFilePost.setTransferResult(VideoFileTransferResultEnum.SUCCESS.getStatus());
            this.convertVideo2Ts(completeVideo);
        } catch (Exception e) {
            log.error("转码文件异常",e);
            updateFilePost.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
        }finally {
            // 如果没有设置 duration，设置为 0
            if(updateFilePost.getDuration() == null) {
                updateFilePost.setDuration(0);
            }
            videoInfoFilePostMapper.updateByUploadIdAndUserId(updateFilePost,videoInfoFilePost.getUploadId(),videoInfoFilePost.getUserId());
            VideoInfoFilePostQuery filePostQuery = new VideoInfoFilePostQuery();
            filePostQuery.setVideoId(videoInfoFilePost.getVideoId());
            filePostQuery.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
            Integer failCount = videoInfoFilePostMapper.selectCount(filePostQuery);
            if(failCount>0){
                VideoInfoPost videoUpdate = new VideoInfoPost();
                videoUpdate.setStatus(VideoStatusEnum.STATUS1.getStatus());
                videoInfoPostMapper.updateByVideoId(videoUpdate,videoInfoFilePost.getVideoId());
                return;
            }
            filePostQuery.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
            Integer transferCount = videoInfoFilePostMapper.selectCount(filePostQuery);
            log.info("转码完成，");
            if(transferCount==0){
                Integer duration = videoInfoFilePostMapper.sumDuration(videoInfoFilePost.getVideoId());
                VideoInfoPost videoUpdate = new VideoInfoPost();
                videoUpdate.setStatus(VideoStatusEnum.STATUS2.getStatus());
                videoUpdate.setDuration(duration);
                log.info("更新视频信息：{}",videoUpdate);
                videoInfoPostMapper.updateByVideoId(videoUpdate,videoInfoFilePost.getVideoId());
            }
        }
    }

    private void convertVideo2Ts(String completeVideo){
        File videoFile = new File(completeVideo);
        File tsFolder = videoFile.getParentFile();
        String codec = ffmpegUtils.getVideoCodec(completeVideo);
        if(Constants.VIDEO_CODE_HEVC.equals(codec)){
            String tempFileName = completeVideo+Constants.VIDEO_CODE_TEMP_FILE_SUFFIX;
            new File(completeVideo).renameTo(new File(tempFileName));
            ffmpegUtils.convertHevc2Mp4(tempFileName,completeVideo);
            new File(tempFileName).delete();
        }
        ffmpegUtils.convertVideo2Ts(tsFolder,completeVideo);
        videoFile.delete();
    }

    @Override
    public PaginationResult<VideoInfoPost> findListByPage(VideoInfoPostQuery query) {
        // 设置默认分页参数
        if (query.getPageNo() == null || query.getPageNo() <= 0) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null || query.getPageSize() <= 0) {
            query.setPageSize(10);
        }

        // 创建 SimplePage 对象用于 MyBatis 分页
        query.setSimplePage(new SimplePage(query.getPageNo(), query.getPageSize()));

        // 查询总数
        Integer totalCount = this.videoInfoPostMapper.selectCountByQuery(query);

        // 计算总页数
        Integer totalPages = (totalCount + query.getPageSize() - 1) / query.getPageSize();

        // 查询数据列表
        List<VideoInfoPost> list = this.videoInfoPostMapper.selectList(query);

        // 封装分页结果
        return PaginationResult.<VideoInfoPost>builder()
                .pageNo(query.getPageNo())
                .pageSize(query.getPageSize())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .list(list)
                .build();
    }

    @Override
    public Integer findCountByParam(VideoInfoPostQuery query) {
        return this.videoInfoPostMapper.selectCountByQuery(query);
    }

    @Override
    public VideoInfoPost getVideoInfoPostByVideoId(String videoId) {
        if (videoId == null || videoId.isEmpty()) {
            throw new BaseException(600, "视频 ID 不能为空");
        }
        return videoInfoPostMapper.selectByVideoId(videoId);
    }

    private Boolean changeVideoInfo(VideoInfoPost videoInfoPost){
        VideoInfoPost dbInfo = this.videoInfoPostMapper.selectByVideoId(videoInfoPost.getVideoId());
        if(!videoInfoPost.getVideoName().equals(dbInfo.getVideoName())
        || !videoInfoPost.getVideoCover().equals(dbInfo.getVideoCover())
        || !videoInfoPost.getTags().equals(dbInfo.getTags())
        || !videoInfoPost.getIntroduction().equals(dbInfo.getIntroduction())){
            return true;
        }
        return false;
    }

    private void union(String dirPath,String toFilePath,Boolean delSource){
        File dir = new File(dirPath);
        if(!dir.exists()){
            throw new RuntimeException("目录不存在");
        }
        File[] fileList = dir.listFiles();
        File targetFile = new File(toFilePath);
        try(RandomAccessFile writeFile = new RandomAccessFile(targetFile,"rw")){
            byte[] b = new byte[1024 *10];
            for(int i = 0;i<fileList.length;i++){
                int len = -1;
                File chunkFile = new File(dirPath+File.separator+i);
                RandomAccessFile readFile = null;
                try{
                    readFile = new RandomAccessFile(chunkFile,"r");
                    while((len = readFile.read(b))!=-1){
                        writeFile.write(b,0,len);
                    }
                }catch(Exception e){
                    log.error("文件不存在",e);
                    throw new RuntimeException("文件不存在");
                }finally {
                    readFile.close();
                }
            }
        }catch(Exception e){
            throw new RuntimeException("合并文件"+dirPath+"异常");
        }finally {
            if (delSource){
                for(int i = 0;i<fileList.length;i++){
                    fileList[i].delete();
                }
            }
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditVideo(String videoId, Integer status,String reason) {
        VideoStatusEnum statusEnum = VideoStatusEnum.getByStatus(status);
        if(statusEnum==null){
            throw new BaseException(600,"状态错误");
        }
        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setStatus(status);
        VideoInfoPostQuery query = new VideoInfoPostQuery();
        query.setStatus(VideoStatusEnum.STATUS2.getStatus());
        query.setVideoId(videoId);
        Integer auditCount = videoInfoPostMapper.updateByParam(videoInfoPost, query);
        if(auditCount==0){
            throw new BaseException(600,"审核失败");
        }

        VideoInfoFilePost videoInfoFilePost= new VideoInfoFilePost();
        videoInfoFilePost.setUpdateType(VideoFileUpdateTypeEnum.NO_UPDATE.getStatus());
        VideoInfoFilePostQuery fileQuery = new VideoInfoFilePostQuery();
        fileQuery.setVideoId(videoId);
        videoInfoFilePostMapper.updateByParam(videoInfoFilePost,fileQuery);
        if(VideoStatusEnum.STATUS4 == statusEnum){
            return;
        }

        VideoInfoPost infoPost = videoInfoPostMapper.selectByVideoId(videoId);
        VideoInfo dbVideoInfo = videoInfoMapper.selectByVideoId(videoId);
        if(dbVideoInfo ==  null){
            SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
            userInfoMapper.updateCoinCountByUserId(infoPost.getUserId(),sysSettingDto.getPostVideoCoinCount());
        }

        //更新发布信息
        VideoInfo videoInfo = CopyTools.copy(infoPost,VideoInfo.class);
        if(dbVideoInfo!=null){
            videoInfo.setCommentCount(dbVideoInfo.getCommentCount());
            videoInfo.setCollectCount(dbVideoInfo.getCollectCount());
            videoInfo.setCoinCount(dbVideoInfo.getCoinCount());
            videoInfo.setDanmuCount(dbVideoInfo.getDanmuCount());
            videoInfo.setLikeCount(dbVideoInfo.getLikeCount());
            videoInfo.setPlayCount(dbVideoInfo.getPlayCount());
        }
        log.info("更新发布信息：{}",videoInfo);
        videoInfoMapper.insertOrUpdate(videoInfo);

        //更新视频文件信息：先删除旧数据，再插入新数据
        VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
        videoInfoFileQuery.setVideoId(videoId);
        videoInfoFileMapper.deleteByParam(videoInfoFileQuery);

        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        List<VideoInfoFilePost> videoInfoFilePostList = videoInfoFilePostMapper.selectList(videoInfoFilePostQuery);

        List<VideoInfoFile> videoInfoFileList = CopyTools.copyList(videoInfoFilePostList,VideoInfoFile.class);
        videoInfoFileMapper.insertBatch(videoInfoFileList);

        List< String> filePathList = redisComponent.getDelFileList(videoId);
        if(filePathList != null){
            for(String filePath : filePathList){
                File file = new File(appConfig.getProjectFolder()+Constants.FILE_FOLDER+filePath);
                if(file.exists()){
                    try{
                        FileUtils.deleteDirectory(file);
                    }catch (Exception e){
                        log.error("删除文件失败",e);
                    }
                }
            }
        }
        redisComponent.cleanDelFileList(videoId);
        esSearchComponent.saveDoc(videoInfo);

    }

}
