package com.example.mhpractice.features.user.controller;

import com.example.mhpractice.features.user.service.AuthService;
import com.example.mhpractice.features.user.service.OtpService;
import com.example.mhpractice.features.user.service.result.LoginResult;
import com.example.mhpractice.features.user.controller.request.RegisterRequest;
import com.example.mhpractice.features.user.controller.request.VerifyOtpRequest;
import com.example.mhpractice.common.http.annotation.StandardReponseBody;
import com.example.mhpractice.features.notification.service.NotificationService;
import com.example.mhpractice.features.user.controller.request.LoginRequest;
import com.example.mhpractice.features.user.controller.request.RefreshTokenRequest;
import com.example.mhpractice.features.user.controller.response.LoginResponse;
import com.example.mhpractice.features.user.models.VerificationToken.OtpPurposes;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@StandardReponseBody
public class AuthController {

    private final AuthService authService;

    private final OtpService otpService;

    private final NotificationService notificationService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        authService.register(request.getEmail(), request.getName(), request.getPassword());

        otpService.requestOtp(request.getEmail(), OtpPurposes.VERIFICATION, generateIpAddress(httpRequest));
    }

    @PostMapping("/verify")
    public boolean verify(@Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {
        boolean verifyOtp = otpService.verifyOtp(request.getEmail(), OtpPurposes.VERIFICATION, request.getOtpCode(),
                generateIpAddress(httpRequest));

        if (verifyOtp) {
            notificationService.sendWelcomeEmail(request.getEmail());
        }

        return verifyOtp;
    }

    @PostMapping("/resend-otp")
    public void resendOtp(@Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {
        otpService.resendOtp(request.getEmail(), OtpPurposes.VERIFICATION, generateIpAddress(httpRequest));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResult result = authService.login(request.getEmail(), request.getPassword());

        setAuthCookie(response, result.getAccessToken());

        return LoginResponse.builder()
                .accessToken(result.getAccessToken())
                .refreshToken(result.getRefreshToken())
                .build();
    }

    @PostMapping("/refresh-token")
    public LoginResponse refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletResponse response) {
        LoginResult result = authService.refreshToken(refreshTokenRequest.getRefreshToken());

        setAuthCookie(response, result.getAccessToken());

        return LoginResponse.builder()
                .accessToken(result.getAccessToken())
                .refreshToken(result.getRefreshToken())
                .build();
    }

    @PostMapping("/logout")
    public void logout(Authentication authentication, HttpServletResponse response) {
        authService.logout(authentication.getName());
        clearAuthCookie(response);
    }

    // ================
    // Private Methods
    // ================

    private void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(15 * 60);
        response.addCookie(cookie);
    }

    private void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String generateIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
