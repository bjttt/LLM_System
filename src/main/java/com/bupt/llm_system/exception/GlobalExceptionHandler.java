package com.bupt.llm_system.exception;

import com.bupt.llm_system.pojo.dto.ResponseMessage;
import com.bupt.llm_system.pojo.dto.ResultCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseMessage<Void> handle(Exception e) {
        return ResponseMessage.error(
                ResultCode.SYSTEM_ERROR,
                "系统异常"
        );
    }
}
