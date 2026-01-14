package com.example.mhpractice.features.user.controller;

import com.example.mhpractice.features.user.service.AuthService;
import com.example.mhpractice.features.user.service.result.LoginResult;
import com.example.mhpractice.features.user.controller.request.RegisterRequest;
import com.example.mhpractice.features.user.controller.request.LoginRequest;
import com.example.mhpractice.features.user.controller.request.RefreshTokenRequest;
import com.example.mhpractice.features.user.controller.response.LoginResponse;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.Cookie;
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
public class AuthController {

    private final AuthService authService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.getEmail(), request.getName(), request.getPassword());
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
}
