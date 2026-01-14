package com.example.mhpractice.features.user.service.impl;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.example.mhpractice.features.user.models.RefreshToken;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.common.exception.ErrorCode;
import com.example.mhpractice.common.security.JwtTokenProvider;
import com.example.mhpractice.features.user.repository.RefreshTokenRepository;
import com.example.mhpractice.features.user.repository.UserRepository;
import com.example.mhpractice.features.user.service.result.LoginResult;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void success_register_new_email() {
        String email = "test@example.com";
        String password = "password";
        String name = "test";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        authService.register(email, name, password);

        verify(userRepository).save(argThat(user -> {
            return user.getEmail().equals(email) && user.getPassword().equals("encodedPassword");
        }));
    }

    @Test
    void fail_register_existing_email() {
        String email = "test@example.com";
        String password = "password";
        String name = "test";

        User existingUser = User.builder()
                .email(email)
                .password("any-password")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        assertThrows(BusinessException.class, () -> authService.register(email, name, password));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void success_login() {
        String email = "test@example.com";
        String password = "password";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        User user = User.builder()
                .id("1L")
                .email(email)
                .password(password)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(email)).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(email)).thenReturn(refreshToken);

        LoginResult loginResult = authService.login(email, password);
        assertNotNull(loginResult);

        assertEquals(accessToken, loginResult.getAccessToken());
        assertEquals(refreshToken, loginResult.getRefreshToken());

        verify(authenticationManager).authenticate(argThat(authentication -> {
            return authentication.getPrincipal().equals(email) && authentication.getCredentials().equals(password);
        }));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void failed_login_invalid_credentials() {
        String email = "test@example.com";
        String password = "password";

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        assertThrows(BusinessException.class, () -> authService.login(email, password));
    }

    @Test
    void refreshToken_WithValidToken_Success() {
        // Given
        String oldToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        User user = User.builder()
                .id("user-123")
                .email("test@example.com")
                .build();

        RefreshToken storedToken = RefreshToken.builder()
                .id("token-id")
                .token(oldToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7)) // ← Not expired!
                .build();

        when(refreshTokenRepository.findByToken(oldToken)).thenReturn(Optional.of(storedToken));
        when(jwtTokenProvider.generateAccessToken(user.getEmail())).thenReturn(newAccessToken);
        when(jwtTokenProvider.generateRefreshToken(user.getEmail())).thenReturn(newRefreshToken);

        // When
        LoginResult result = authService.refreshToken(oldToken);

        // Then
        assertNotNull(result);
        assertEquals(newAccessToken, result.getAccessToken());
        assertEquals(newRefreshToken, result.getRefreshToken());
        verify(refreshTokenRepository).delete(storedToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_WithExpiredToken_ThrowsException() {
        // Given
        String expiredToken = "expired-token";

        RefreshToken storedToken = RefreshToken.builder()
                .token(expiredToken)
                .user(User.builder().id("user-123").email("test@example.com").build())
                .expiresAt(LocalDateTime.now().minusDays(1)) // ← Expired!
                .build();

        when(refreshTokenRepository.findByToken(expiredToken)).thenReturn(Optional.of(storedToken));

        // When & Then
        assertThrows(BusinessException.class, () -> authService.refreshToken(expiredToken));
        verify(refreshTokenRepository, never()).delete(storedToken);
    }
}