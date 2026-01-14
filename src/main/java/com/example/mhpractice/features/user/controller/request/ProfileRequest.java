package com.example.mhpractice.features.user.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 60, message = "Name too long")
    private String name;
}
