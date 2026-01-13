package com.example.mhpractice.common.exception;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;

}
