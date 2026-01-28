package com.example.mhpractice.features.user.service.impl;

import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.common.exception.ErrorCode;
import com.example.mhpractice.features.user.models.RefreshToken;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.repository.RefreshTokenRepository;
import com.example.mhpractice.features.user.repository.UserRepository;
import com.example.mhpractice.features.user.service.AuthService;
import com.example.mhpractice.features.user.service.result.LoginResult;
import com.example.mhpractice.common.security.JwtTokenProvider;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void register(String email, String name, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .status(User.Status.ACTIVE)
                .build();

        userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public LoginResult login(String email, String password) {
        // User user = userRepository.findByEmail(email)
        // .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // if (!passwordEncoder.matches(password, user.getPassword())) {
        // throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        // }

        // Create authentication token
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        // Authentication manager authenticates the user
        authenticationManager.authenticate(authToken);

        // Cannot set authentication in security context here, since is per thread
        // If set here, once this request done, the authentication will be lost
        // Set in JwtAuthenticationFilter, since every request will go through filter
        // and will have set the authentication in security context

        String accessToken = jwtTokenProvider.generateAccessToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(userRepository.findByEmail(email)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public LoginResult refreshToken(String refreshToken) {
        try {

            RefreshToken storedRefreshToken = refreshTokenRepository
                    .findByToken(refreshToken)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

            // 2. Check if expired
            if (storedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }

            String email = storedRefreshToken.getUser().getEmail();
            String newAccessToken = jwtTokenProvider.generateAccessToken(email);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

            // Delete old token (one-time use)
            refreshTokenRepository.delete(storedRefreshToken);

            // Create new token
            RefreshToken newToken = RefreshToken.builder()
                    .user(storedRefreshToken.getUser())
                    .token(newRefreshToken)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();
            refreshTokenRepository.save(newToken);

            return LoginResult.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    @Override
    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        refreshTokenRepository.deleteByUser(user);
    }

}
