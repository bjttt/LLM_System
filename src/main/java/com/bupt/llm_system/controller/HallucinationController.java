package com.bupt.llm_system.controller;

import com.bupt.llm_system.pojo.dto.ResponseMessage;
import com.bupt.llm_system.pojo.internal.HallucinationRequest;
import com.bupt.llm_system.service.IHallucinationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/llm")
public class HallucinationController {

    private final IHallucinationService hallucinationService;

    public HallucinationController(IHallucinationService hallucinationService) {
        this.hallucinationService = hallucinationService;
    }

    @PostMapping("/hallucination")
    public ResponseMessage<Object> detect(@RequestBody HallucinationRequest req) {
        return hallucinationService.detect(req);
    }
}

