package com.example.mhpractice.features.user.service.result;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResult {
    private String accessToken;
    private String refreshToken;
}
