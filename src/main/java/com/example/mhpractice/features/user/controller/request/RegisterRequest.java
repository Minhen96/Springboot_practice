package com.example.mhpractice.features.user.controller.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String name;
    private String password;
}
