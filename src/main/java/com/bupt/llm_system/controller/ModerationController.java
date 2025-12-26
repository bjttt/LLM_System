package com.bupt.llm_system.controller;

import com.bupt.llm_system.pojo.dto.ResponseMessage;
import com.bupt.llm_system.pojo.dto.ResultCode;
import com.bupt.llm_system.pojo.internal.ModerationRequest;
import com.bupt.llm_system.service.ILlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/moderation")
public class ModerationController {

    private final ILlmService llmService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ModerationController(ILlmService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/text")
    public ResponseMessage<Object> classify(@RequestBody ModerationRequest req) {

        try {
            String raw = llmService.classifyText(req.getText());
            JsonNode node = mapper.readTree(raw);

            return ResponseMessage.success(
                    node.path("labels")
            );

        } catch (Exception e) {
            return ResponseMessage.error(
                    ResultCode.MODEL_ERROR,
                    "模型结果解析失败"
            );
        }
    }
}
