package com.example.mhpractice.features.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mhpractice.common.http.annotation.StandardReponseBody;
import com.example.mhpractice.features.user.controller.request.ProfileRequest;
import com.example.mhpractice.features.user.controller.response.UserProfileResponse;
import com.example.mhpractice.features.user.service.UserService;
import com.example.mhpractice.features.user.service.result.UserProfileResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@StandardReponseBody
public class UserProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    public UserProfileResponse getUserProfile(Authentication authentication) {
        UserProfileResult userProfileResult = userService.getUserProfile(authentication.getName());
        return UserProfileResponse.builder()
                .id(userProfileResult.getId())
                .email(userProfileResult.getEmail())
                .name(userProfileResult.getName())
                .build();
    }

    @PutMapping("/profile")
    public void updateUserProfile(@Valid @RequestBody ProfileRequest profileRequest, Authentication authentication) {
        userService.updateUserProfile(authentication.getName(), profileRequest.getName());
    }
}
