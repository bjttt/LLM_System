package com.bupt.llm_system.pojo.internal;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class MultimodalModerationForm {
    private String text;
    private List<String> imageUrls;
    private List<MultipartFile> files;
    private Boolean stream = false;
}

