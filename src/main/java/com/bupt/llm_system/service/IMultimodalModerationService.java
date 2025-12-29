package com.bupt.llm_system.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface IMultimodalModerationService {
    String checkContent(String text, List<String> images);
    void checkContentStream(String text, List<String> images, SseEmitter emitter);
}
