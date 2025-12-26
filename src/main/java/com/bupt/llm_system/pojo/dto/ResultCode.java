package com.bupt.llm_system.pojo.dto;

public interface ResultCode {

    Integer SUCCESS = 0;
    Integer PARAM_ERROR = 1001;
    Integer MODEL_ERROR = 2001;
    Integer IMAGE_ERROR = 3001;
    Integer SYSTEM_ERROR = 5000;
}
