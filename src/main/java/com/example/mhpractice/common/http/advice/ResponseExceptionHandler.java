package com.example.mhpractice.common.http.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;

import com.example.mhpractice.common.exception.ErrorCode;
import com.example.mhpractice.common.exception.ErrorResponse;
// import com.example.mhpractice.common.http.advice.StandardResponse; // same package, no need to import

@RestControllerAdvice
public class ResponseExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardResponse<Void>> handleException(AuthenticationException e) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.USER_NOT_FOUND.getCode())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(StandardResponse.error(errorResponse));
    }
}
