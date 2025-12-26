package com.bupt.llm_system.service;

import com.bupt.llm_system.pojo.internal.ImageDetectResult;
import org.springframework.web.multipart.MultipartFile;

public interface IImageDetectService {
    ImageDetectResult detect(MultipartFile image);
}
