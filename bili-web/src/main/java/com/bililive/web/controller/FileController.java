package com.bililive.web.controller;

import com.bililive.Enum.DateTimePatternEnum;
import com.bililive.annotation.GlobalInterceptor;
import com.bililive.component.RedisComponent;
import com.bililive.entity.config.AppConfig;
import com.bililive.entity.constants.Constants;
import com.bililive.mapper.VideoInfoFileMapper;
import com.bililive.pojo.*;
import com.bililive.utils.DateUtils;
import com.bililive.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private VideoInfoFileMapper videoInfoFileMapper;

    /**
     * 上传图片
     */
    @RequestMapping("/uploadImage")
    public Result<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "createThumbnail", defaultValue = "false") boolean createThumbnail) {
        
        log.info("开始上传图片 - 文件名：{}, 大小：{} bytes", file.getOriginalFilename(), file.getSize());
        
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new BaseException("上传文件不能为空");
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        if (!isValidImageExtension(fileExtension)) {
            throw new BaseException("只支持 JPG、PNG、GIF 格式图片");
        }
        
        // 检查文件大小（限制 5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BaseException("图片大小不能超过 5MB");
        }
        
        try {
            // 验证是否为有效的图片
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new BaseException("文件不是有效的图片格式");
            }
            
            // 生成文件路径
            String uploadDir = getUploadDirectory();
            String subDir = generateSubDirectory();
            String fullDir = uploadDir + File.separator + subDir;
            
            // 创建目录
            File dir = new File(fullDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成唯一文件名
            String fileName = generateUniqueFileName(fileExtension);
            String filePath = fullDir + File.separator + fileName;
            
            // 保存文件
            file.transferTo(new File(filePath));
            log.info("图片保存成功：{}", filePath);
            
            // 如果需要生成缩略图
            if (createThumbnail) {
                generateThumbnail(image, fullDir, fileName, fileExtension);
            }
            
            // 返回相对路径（用于数据库存储）
            // 格式：2026/03/07/xxx.png
            String relativePath = subDir + "/" + fileName;
            log.info("返回相对路径：{}", relativePath);
            
            return Result.success(relativePath, "上传成功");
            
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw new BaseException("图片上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取资源文件（图片等）
     */
    @RequestMapping("/getResource")
    public void getResource(@RequestParam String sourceName,
                            javax.servlet.http.HttpServletResponse response) {
        log.info("获取资源文件：{}", sourceName);

        if (sourceName == null || sourceName.trim().isEmpty()) {
            throw new BaseException("资源名称不能为空");
        }

        try {
            String uploadDir = getUploadDirectory();
            String filePath = uploadDir + File.separator + sourceName;

            File file = new File(filePath);
            if (!file.exists()) {
                throw new BaseException("资源文件不存在");
            }

            // 设置响应头
            String contentType = Files.probeContentType(Paths.get(filePath));
            if (contentType != null) {
                response.setContentType(contentType);
            } else {
                response.setContentType("application/octet-stream");
            }

            // 设置缓存
            response.setHeader("Cache-Control", "max-age=31536000");

            // 输出文件流
            Files.copy(Paths.get(filePath), response.getOutputStream());
            response.getOutputStream().flush();

        } catch (IOException e) {
            log.error("获取资源文件失败", e);
            throw new BaseException("获取资源文件失败：" + e.getMessage());
        }
    }

    /**
     * 删除图片
     */
    @RequestMapping("/deleteImage")
    public Result deleteImage(@RequestParam String sourceName) {
        log.info("删除图片：{}", sourceName);

        if (sourceName == null || sourceName.trim().isEmpty()) {
            throw new BaseException("资源名称不能为空");
        }

        try {
            String uploadDir = getUploadDirectory();
            String filePath = uploadDir + File.separator + sourceName;

            File file = new File(filePath);
            if (file.exists()) {
                // 删除原图
                file.delete();
                log.info("已删除图片：{}", filePath);

                // 尝试删除缩略图
                String thumbnailPath = getThumbnailPath(filePath);
                File thumbnailFile = new File(thumbnailPath);
                if (thumbnailFile.exists()) {
                    thumbnailFile.delete();
                    log.info("已删除缩略图：{}", thumbnailPath);
                }

                return Result.success("删除成功");
            } else {
                throw new BaseException("图片不存在");
            }

        } catch (Exception e) {
            log.error("删除图片失败", e);
            throw new BaseException("删除图片失败：" + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 上传单个文件
     */
    private Map<String, String> uploadSingleFile(MultipartFile file, boolean createThumbnail) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        if (!isValidImageExtension(fileExtension)) {
            throw new BaseException("不支持的文件格式：" + originalFilename);
        }

        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new BaseException("无效的图片文件：" + originalFilename);
        }

        String uploadDir = getUploadDirectory();
        String subDir = generateSubDirectory();
        String fullDir = uploadDir + File.separator + subDir;

        File dir = new File(fullDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = generateUniqueFileName(fileExtension);
        String filePath = fullDir + File.separator + fileName;

        file.transferTo(new File(filePath));

        if (createThumbnail) {
            generateThumbnail(image, fullDir, fileName, fileExtension);
        }

        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put("url", "/api/file/getResource?sourceName=" + subDir + "/" + fileName);
        fileInfo.put("fileName", fileName);
        fileInfo.put("originalName", originalFilename);
        fileInfo.put("size", String.valueOf(file.getSize()));

        return fileInfo;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 验证图片扩展名
     */
    private boolean isValidImageExtension(String extension) {
        return ".jpg".equals(extension) || ".jpeg".equals(extension) ||
                ".png".equals(extension) || ".gif".equals(extension) ||
                ".bmp".equals(extension) || ".webp".equals(extension);
    }

    /**
     * 获取上传目录
     */
    private String getUploadDirectory() {
        String baseDir = appConfig.getProjectFolder();
        if (baseDir == null || baseDir.trim().isEmpty()) {
            baseDir = System.getProperty("java.io.tmpdir") + File.separator + "bililive";
        }
        return baseDir + File.separator + "uploads" + File.separator + "images";
    }

    /**
     * 生成子目录（按日期）
     */
    private String generateSubDirectory() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(new Date());
    }

    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 生成缩略图
     */
    private String generateThumbnail(BufferedImage originalImage, String dir, String fileName, String extension) {
        try {
            int thumbnailWidth = 200;
            int thumbnailHeight = 200;

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // 保持宽高比
            double ratio = Math.min((double) thumbnailWidth / originalWidth,
                    (double) thumbnailHeight / originalHeight);
            int width = (int) (originalWidth * ratio);
            int height = (int) (originalHeight * ratio);

            BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            thumbnail.getGraphics().drawImage(
                    originalImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH),
                    0, 0, null
            );

            // 缩略图文件名
            String thumbnailFileName = "thumb_" + fileName;
            String thumbnailPath = dir + File.separator + thumbnailFileName;

            ImageIO.write(thumbnail, extension.replace(".", ""), new File(thumbnailPath));
            log.info("生成缩略图：{}", thumbnailPath);

            return "/api/file/getResource?sourceName=" +
                    dir.substring(getUploadDirectory().length() + 1) + "/" + thumbnailFileName;

        } catch (IOException e) {
            log.error("生成缩略图失败", e);
            return null;
        }
    }

    /**
     * 获取缩略图路径
     */
    private String getThumbnailPath(String imagePath) {
        int lastDot = imagePath.lastIndexOf(".");
        if (lastDot == -1) {
            return imagePath + "_thumb";
        }
        String name = imagePath.substring(0, lastDot);
        String ext = imagePath.substring(lastDot);
        return name + "_thumb" + ext;
    }

    /**
     * 预上传视频（获取上传 ID）
     */
    @RequestMapping("/preUploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> preUploadVideo(
            @NotEmpty String fileName,
            @NotNull Integer chunks,
            HttpServletRequest request) {
        
        log.info("预上传视频 - fileName: {}, chunks: {}", fileName, chunks);

        // 获取当前登录用户
        TokenUserInfoDto userInfo = getTokenUserInfoDto(request);
        String uploadId = redisComponent.savePreview(userInfo.getUserId(), fileName, chunks);
        return Result.success(uploadId);
    }

    @RequestMapping("/uploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> uploadVideo(
            @NotNull MultipartFile chunkFile,
            @NotNull Integer chunkIndex,
            @NotNull String uploadId,
            HttpServletRequest request) throws IOException {
        log.info("上传视频分片 - chunkIndex: {}, uploadId: {}", chunkIndex, uploadId);
        TokenUserInfoDto userInfo = getTokenUserInfoDto(request);
        UploadingFileDto fileDto = redisComponent.getPreview(userInfo.getUserId(), uploadId);
        if (fileDto == null) {
            throw new BaseException("文件不存在请重新上传");
        }
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        if (fileDto.getFileSize() > sysSettingDto.getVideoSize()*1024*1024) {
            throw new BaseException("文件超过大小限制");
        }
        //判断分片
        if( (chunkIndex -1)>fileDto.getChunkIndex() || chunkIndex > fileDto.getChunks() -1) {
            throw new BaseException(600,"分片错误");
        }

        String folder = appConfig.getProjectFolder()+Constants.FILE_FOLDER+Constants.FILE_FOLDER_TEMP+fileDto.getFilePath();
        File targetFile = new File(folder+"/"+chunkIndex);
        chunkFile.transferTo(targetFile);
        fileDto.setChunkIndex(chunkIndex);
        fileDto.setFileSize(fileDto.getFileSize()+chunkFile.getSize());
        redisComponent.updatePreview(userInfo.getUserId(),fileDto);
        return Result.success();
    }
    @RequestMapping("/delUploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result<String> deUploadVideo(
            @NotNull String uploadId,
            HttpServletRequest request) throws IOException{
        log.info("删除上传视频分片 - uploadId: {}", uploadId);
        TokenUserInfoDto userInfo = getTokenUserInfoDto(request);
        UploadingFileDto fileDto = redisComponent.getPreview(userInfo.getUserId(), uploadId);
        if (fileDto == null) {
            throw new BaseException("文件不存在请重新上传");
        }
        redisComponent.delPreview(userInfo.getUserId(), uploadId);
        FileUtils.deleteDirectory(new File(appConfig.getProjectFolder()+Constants.FILE_FOLDER+Constants.FILE_FOLDER_TEMP+fileDto.getFilePath()));
        return Result.success(uploadId);
    }

    @RequestMapping("/uploadImages")
    public Result<String> uploadImages(@NotNull MultipartFile file, @NotNull Boolean createThumbnail) throws  IOException{
        String day = DateUtils.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        String folder = appConfig.getProjectFolder()+Constants.FILE_FOLDER+Constants.FILE_COVER+day;
        File folderFile = new File(folder);
        if(!folderFile.exists()){
            folderFile.mkdirs();
        }
        String fileName = file.getOriginalFilename();
        String fileSuffix = StringTools.getFileSuffix(fileName);
        String realFileName = UUID.randomUUID().toString()+fileSuffix;
        String filePath = folder+"/"+realFileName;
        file.transferTo(new File(filePath));
        
        // 生成缩略图
        if (createThumbnail != null && createThumbnail){
            generateVideoThumbnail(filePath, folder, realFileName, fileSuffix);
        }
        
        return Result.success(Constants.FILE_COVER+day+"/"+realFileName);
    }
    @RequestMapping("/videoResource/{fileId}/**")
    public void videoResource(HttpServletResponse response, @PathVariable @NotEmpty String fileId, HttpServletRequest request){
        VideoInfoFile videoInfoFile = videoInfoFileMapper.getVideoInfoFileByFileId(fileId);
        if (videoInfoFile == null) {
            log.warn("视频文件不存在：{}", fileId);
            throw new BaseException("视频文件不存在");
        }
        
        String filePath = videoInfoFile.getFilePath();
        boolean index = readFile(response, filePath, fileId, request);

        if (index) {
            VideoPlayInfo videoPlayInfo = new VideoPlayInfo();
            videoPlayInfo.setVideoId(videoInfoFile.getVideoId());
            videoPlayInfo.setUserId(videoInfoFile.getUserId());
            videoPlayInfo.setFileIndex(videoInfoFile.getFileIndex());
            TokenUserInfoDto userInfo = getTokenUserInfoFromCookie(request);
            if(userInfo != null){
                videoPlayInfo.setUserId(userInfo.getUserId());
            }
            redisComponent.addVideoPlay(videoPlayInfo);
        }
    }
    /**
     * 读取并输出文件内容
     */
    private boolean readFile(HttpServletResponse response, String filePath, String fileId, HttpServletRequest request) {
        // 获取请求路径中的文件名部分（如 /0000.ts）
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String basePath = contextPath + "/file/videoResource/" + fileId;
        String fileName = requestUri.substring(basePath.length());
        Boolean index = false;
        // 如果没有指定文件名，默认返回 index.m3u8
        if (fileName == null || fileName.isEmpty() || "/".equals(fileName)) {
            fileName = "/" + Constants.M3U8_NAME;
            index = true;
        }
        
        log.info("视频资源：{}, 文件名：{}", filePath, fileName);
        
        // 拼接完整路径：项目根目录 + 文件目录 + 视频目录 + 文件路径 + 文件名
        String fullPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + filePath + fileName;
        
        File file = new File(fullPath);
        if (!file.exists()) {
            log.warn("文件不存在：{}", fullPath);
            throw new BaseException("文件不存在");
        }

        try {
            // 设置响应类型
            String contentType = Files.probeContentType(Paths.get(fullPath));
            if (contentType != null) {
                response.setContentType(contentType);
            } else {
                response.setContentType("application/octet-stream");
            }

            // 设置缓存控制（对于 HLS 流媒体，禁用缓存）
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // 输出文件流
            Files.copy(Paths.get(fullPath), response.getOutputStream());
            response.getOutputStream().flush();

            log.info("文件读取成功：{}", fullPath);

        } catch (IOException e) {
            log.error("读取文件失败：{}", fullPath, e);
            throw new BaseException("读取文件失败：" + e.getMessage());
        }
        return index;
    }
    /**
     * 生成视频封面缩略图
     */
    private void generateVideoThumbnail(String imagePath, String folder, String fileName, String fileSuffix) {
        try {
            // 读取原图
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                log.warn("图片文件不存在：{}", imagePath);
                return;
            }
            
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                log.warn("无法读取图片：{}", imagePath);
                return;
            }
            
            // 计算缩略图尺寸（保持宽高比）
            int thumbnailWidth = 320;
            int thumbnailHeight = 180;
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // 保持宽高比缩放
            double widthRatio = (double) thumbnailWidth / originalWidth;
            double heightRatio = (double) thumbnailHeight / originalHeight;
            double scale = Math.min(widthRatio, heightRatio);
            
            int newWidth = (int) (originalWidth * scale);
            int newHeight = (int) (originalHeight * scale);
            
            // 创建缩略图
            BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            thumbnail.getGraphics().drawImage(
                    originalImage.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH),
                    0, 0, null
            );
            
            // 生成缩略图文件名
            String thumbnailFileName = getThumbnailFileName(fileName);
            String thumbnailPath = folder + File.separator + thumbnailFileName;
            
            // 保存缩略图
            ImageIO.write(thumbnail, fileSuffix, new File(thumbnailPath));
            log.info("生成视频封面缩略图：{}", thumbnailPath);
            
        } catch (IOException e) {
            log.error("生成视频封面缩略图失败", e);
        }
    }
    
    /**
     * 获取缩略图文件名
     */
    private String getThumbnailFileName(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) {
            return fileName + "_thumb";
        }
        String name = fileName.substring(0, lastDot);
        String ext = fileName.substring(lastDot);
        return name + "_thumb" + ext;
    }
    
    /**
     * 获取当前登录用户
     */
    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request){
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
    protected TokenUserInfoDto getTokenUserInfoFromCookie(HttpServletRequest request){
        String token = null;
        Cookie[] cookies = request.getCookies();
        if(cookies==null){
            return null;
        }
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(Constants.TOKEN_WEB)){
                token = cookie.getValue();
            }
        }
        if(token==null){
            return null;
        }
        return redisComponent.getTokenInfo(token);
    }
}
