package com.bupt.llm_system.service.impl;

import com.bupt.llm_system.pojo.internal.ImageDetectResult;
import com.bupt.llm_system.service.IImageDetectService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageDetectService implements IImageDetectService {

    @Override
    public ImageDetectResult detect(MultipartFile image) {

        ImageDetectResult result = new ImageDetectResult();
        result.setIsFake(true);
        result.setConfidence(0.91);
        result.setExplanation("检测到生成图像特征（示例实现）");

        return result;
    }
}
