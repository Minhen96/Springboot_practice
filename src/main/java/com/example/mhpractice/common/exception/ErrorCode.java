package com.example.mhpractice.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_TOKEN("USER_401", "Invalid token"),
    REFRESH_TOKEN_EXPIRED("USER_401", "Refresh token expired"),
    INVALID_CREDENTIALS("USER_401", "Invalid credentials"),
    USER_NOT_FOUND("USER_404", "User not found"),
    USER_ALREADY_EXISTS("USER_409", "User already exists"),

    OTP_NOT_FOUND("OTP_404", "OTP not found"),
    INVALID_OTP("OTP_401", "Invalid OTP"),
    OTP_EXPIRED("OTP_401", "OTP expired"),
    OTP_MAX_ATTEMPTS_EXCEEDED("OTP_401", "OTP max attempts exceeded"),
    OTP_MAX_RESEND_EXCEEDED("OTP_401", "OTP max resend exceeded"),
    OTP_REQUEST_TOO_FREQUENT("OTP_401", "OTP request too frequent"),

    EMAIL_SERVER_ERROR("EMAIL_500", "Email server error");

    private final String code;
    private final String message;

}
