package com.campus.campussharebackend.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private String message;  // 异常信息

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }
}