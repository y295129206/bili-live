package com.bililive.admin.controller;

import com.bililive.entity.config.AppConfig;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
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

    /**
     * 批量上传图片
     */
    @RequestMapping("/uploadImages")
    public Result<Map<String, Object>> uploadImages(
            @RequestParam("files") MultipartFile[] files) {

        log.info("批量上传图片，数量：{}", files.length);

        if (files == null || files.length == 0) {
            throw new BaseException("上传文件不能为空");
        }

        if (files.length > 10) {
            throw new BaseException("一次最多上传 10 张图片");
        }

        Map<String, Object> result = new HashMap<>();
        java.util.List<Map<String, String>> successList = new java.util.ArrayList<>();
        java.util.List<String> failList = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Map<String, String> fileInfo = uploadSingleFile(file, false);
                successList.add(fileInfo);
            } catch (Exception e) {
                log.error("图片 {} 上传失败", file.getOriginalFilename(), e);
                failList.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        result.put("success", successList);
        result.put("fail", failList);
        result.put("total", files.length);
        result.put("successCount", successList.size());
        result.put("failCount", failList.size());

        return Result.success(result);
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
}
