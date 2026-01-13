package com.example.mhpractice.features.user.controller;

import com.example.mhpractice.features.user.service.AuthService;
import com.example.mhpractice.features.user.service.result.LoginResult;
import com.example.mhpractice.features.user.controller.request.RegisterRequest;
import com.example.mhpractice.features.user.controller.request.LoginRequest;
import com.example.mhpractice.features.user.controller.response.LoginResponse;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.getEmail(), request.getPassword());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.getEmail(), request.getPassword());
        return LoginResponse.builder()
                .accessToken(result.getAccessToken())
                .refreshToken(result.getRefreshToken())
                .build();
    }
}
