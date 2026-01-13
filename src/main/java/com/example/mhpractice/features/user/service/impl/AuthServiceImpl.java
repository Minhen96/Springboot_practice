package com.example.mhpractice.features.user.service.impl;

import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.repository.UserRepository;
import com.example.mhpractice.features.user.service.AuthService;
import com.example.mhpractice.features.user.service.result.LoginResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void register(String email, String password) {
        User user = User.builder()
                .email(email)
                .password(password)
                .build();
        userRepository.save(user);
    }

    @Override
    public LoginResult login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        return LoginResult.builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .build();
    }
}
