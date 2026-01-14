package com.example.mhpractice.common.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.mhpractice.features.user.repository.OtpResendTrackingRepository;
import com.example.mhpractice.features.user.repository.VerificationTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CleanupService {

    private final VerificationTokenRepository verificationTokenRepository;

    private final OtpResendTrackingRepository otpResendTrackingRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        verificationTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
        otpResendTrackingRepository.deleteAllByResetAtBefore(LocalDateTime.now().minusHours(24));
    }

}
