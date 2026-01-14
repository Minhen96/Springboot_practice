package com.example.mhpractice.features.user.controller.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
