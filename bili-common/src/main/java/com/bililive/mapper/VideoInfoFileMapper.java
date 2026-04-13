package com.bililive.mapper;

import com.bililive.pojo.VideoInfoFile;
import com.bililive.pojo.VideoInfoFileQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频文件信息 Mapper 接口
 */
@Mapper
public interface VideoInfoFileMapper {

    /**
     * 根据视频 ID 查询文件列表
     */
    List<VideoInfoFile> selectByVideoId(@Param("videoId") String videoId);

    /**
     * 根据文件 ID 查询文件信息
     */
    VideoInfoFile getVideoInfoFileByFileId(@Param("fileId") String fileId);

    /**
     * 根据查询条件获取文件列表
     */
    List<VideoInfoFile> selectList(VideoInfoFileQuery query);

    /**
     * 插入视频文件信息
     */
    void insert(VideoInfoFile videoInfoFile);

    /**
     * 根据视频 ID 删除文件信息
     */
    void deleteByVideoId(@Param("videoId") String videoId);

    /**
     * 根据上传 ID 删除文件信息
     */
    void deleteByUploadId(@Param("uploadId") String uploadId);

    /**
     * 根据视频 ID 和文件索引删除文件信息
     */
    void deleteByVideoIdAndIndex(@Param("videoId") String videoId, @Param("fileIndex") Integer fileIndex);

    /**
     * 获取文件总数
     */
    Integer selectCount(VideoInfoFileQuery query);

    /**
     * 根据上传 ID 和用户 ID 更新文件信息
     */
    void updateByUploadIdAndUserId(VideoInfoFile updateFilePost, String uploadId, String userId);

    /**
     * 根据查询条件更新视频文件信息
     */
    Integer updateByParam(@Param("videoInfoFile") VideoInfoFile videoInfoFile, @Param("query") VideoInfoFileQuery query);

    /**
     * 根据查询条件删除视频文件信息
     */
    Integer deleteByParam(VideoInfoFileQuery query);

    /**
     * 批量插入视频文件信息
     */
    void insertBatch(@Param("list") List<VideoInfoFile> videoInfoFileList);

}
