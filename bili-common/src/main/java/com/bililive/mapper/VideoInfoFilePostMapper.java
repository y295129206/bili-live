package com.bililive.mapper;

import com.bililive.pojo.VideoInfoFilePost;
import com.bililive.pojo.VideoInfoFilePostQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频文件上传信息 Mapper 接口
 */
@Mapper
public interface VideoInfoFilePostMapper {

    /**
     * 根据视频 ID 查询文件列表
     */
    List<VideoInfoFilePost> selectByVideoId(@Param("videoId") String videoId);

    /**
     * 根据查询条件获取文件列表
     */
    List<VideoInfoFilePost> selectList(VideoInfoFilePostQuery query);

    /**
     * 插入视频文件信息
     */
    void insert(VideoInfoFilePost videoInfoFilePost);

    /**
     * 批量插入视频文件信息
     */
    void batchInsert(@Param("list") List<VideoInfoFilePost> videoInfoFilePosts);

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
    Integer selectCount(VideoInfoFilePostQuery query);

    void deleteBatchByFileId(List<String> deleteFileIdList, String userId);

    void insertOrUpdateBatch(List<VideoInfoFilePost> uploadFileList);

    void updateByUploadIdAndUserId(VideoInfoFilePost updateFilePost, String uploadId, String userId);

    Integer sumDuration(String videoId);

    /**
     * 根据查询条件更新视频文件信息
     */
    Integer updateByParam(@Param("videoInfoFilePost") VideoInfoFilePost videoInfoFilePost, @Param("query") VideoInfoFilePostQuery query);
}
