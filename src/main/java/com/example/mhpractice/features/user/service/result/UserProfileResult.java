package com.example.mhpractice.features.user.service.result;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResult {
    private String email;
    private String name;
}
