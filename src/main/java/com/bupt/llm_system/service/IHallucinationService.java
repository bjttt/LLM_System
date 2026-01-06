package com.bupt.llm_system.service;

import com.bupt.llm_system.pojo.internal.HallucinationRequest;
import com.bupt.llm_system.pojo.dto.ResponseMessage;

public interface IHallucinationService {
    ResponseMessage<Object> detect(HallucinationRequest req);
}

