package com.bupt.llm_system.pojo.internal;

import lombok.Data;

@Data
public class ImageDetectResult {

    private Boolean isFake;
    private Double confidence;
    private String explanation;
}
