package com.example.mhpractice.features.user.service.impl;

import org.springframework.stereotype.Service;

import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.common.exception.ErrorCode;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.repository.UserRepository;
import com.example.mhpractice.features.user.service.UserService;
import com.example.mhpractice.features.user.service.result.UserProfileResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Use email or name to get the user profile
     * 
     * @param email
     * @param name
     */
    @Override
    public UserProfileResult getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserProfileResult.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    @Override
    public void updateUserProfile(String email, String name) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setName(name);
        userRepository.save(user);
    }
}
