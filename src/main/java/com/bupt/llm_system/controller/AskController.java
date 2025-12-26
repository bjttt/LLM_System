package com.bupt.llm_system.controller;

import com.bupt.llm_system.pojo.dto.ResponseMessage;
import com.bupt.llm_system.pojo.dto.ResultCode;
import com.bupt.llm_system.pojo.internal.AskRequest;
import com.bupt.llm_system.service.ILlmService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/llm")
public class AskController {

    private final ILlmService llmService;

    public AskController(ILlmService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/ask")
    public ResponseMessage<String> ask(@RequestBody AskRequest req) {

        try {
            String answer = llmService.ask(req.getQuestion());
            return ResponseMessage.success(answer);

        } catch (Exception e) {
            return ResponseMessage.error(ResultCode.MODEL_ERROR, "模型调用失败");
        }
    }
}
