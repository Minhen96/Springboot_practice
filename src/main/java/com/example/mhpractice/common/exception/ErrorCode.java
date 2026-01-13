package com.example.mhpractice.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("USER_404", "User not found");

    private final String code;
    private final String message;

}
