package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadingFileDto {
    private static final long serialVersionUID = 844272933084899283L;
    private String uploadId;
    private String fileName;
    private Integer chunks;
    private Integer chunkIndex=0;
    private Long fileSize = 0L;
    private String filePath;
}
