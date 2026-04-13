package com.bililive.entity.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppConfig {
    @Value("${project.folder:}")
    private String projectFolder;
    @Value("${admin.account:}")
    private String adminAccount;
    @Value("${admin.password:}")
    private String adminPassword;
    @Value("${ffmpeg.showLog:false}")
    private Boolean showFFmpegLog;
    @Value("${es.host.port:127.0.0.1:9200}")
    private String esHostPort;
    @Value("${es.index.video.name:bililive_video}")
    private String esIndexVideoName;
}
