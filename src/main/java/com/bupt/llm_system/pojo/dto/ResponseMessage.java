package com.bupt.llm_system.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ResponseMessage<T> success(T data) {
        return new ResponseMessage<>(ResultCode.SUCCESS, "success", data);
    }

    public static <T> ResponseMessage<T> success(String message, T data) {
        return new ResponseMessage<>(ResultCode.SUCCESS, message, data);
    }

    public static <T> ResponseMessage<T> error(Integer code, String message) {
        return new ResponseMessage<>(code, message, null);
    }
}
