package com.bililive.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPlayInfo {
    private static final long serialVersionUID = 1L;
    private String videoId;
    private String userId;
    private Integer fileIndex;
}
