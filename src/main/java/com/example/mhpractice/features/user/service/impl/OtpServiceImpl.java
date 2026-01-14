package com.example.mhpractice.features.user.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.common.exception.ErrorCode;
import com.example.mhpractice.features.notification.service.NotificationService;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.models.VerificationToken;
import com.example.mhpractice.features.user.models.VerificationToken.OtpPurposes;
import com.example.mhpractice.features.user.repository.UserRepository;
import com.example.mhpractice.features.user.repository.VerificationTokenRepository;
import com.example.mhpractice.features.user.service.OtpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final UserRepository userRepository;

    private final VerificationTokenRepository verificationTokenRepository;

    private final NotificationService notificationService;

    @Override
    public void requestOtp(String email, OtpPurposes purpose, String ipAddress) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String otpCode = generateOtpCode();
        String hashedOtp = BCrypt.hashpw(otpCode, BCrypt.gensalt(10));

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .otpCode(hashedOtp)
                .purpose(purpose)
                .attemptCount(0)
                .maxAttempts(3)
                .used(false)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        verificationTokenRepository.save(verificationToken);

        notificationService.sendOtpEmail(email, otpCode);
    }

    @Transactional
    @Override
    public boolean verifyOtp(String email, OtpPurposes purpose, String otpCode, String ipAddress) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found"));

        VerificationToken verificationToken = verificationTokenRepository.findByUserAndPurpose(user, purpose)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_NOT_FOUND, "OTP not found"));

        verificationToken.setAttemptCount(verificationToken.getAttemptCount() + 1);

        if (verificationToken.getAttemptCount() > verificationToken.getMaxAttempts()) {
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        if (verificationToken.isUsed()) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        if (!BCrypt.checkpw(otpCode, verificationToken.getOtpCode())) {
            verificationTokenRepository.save(verificationToken);
            throw new BusinessException(ErrorCode.INVALID_OTP);
        }

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        user.setEmailVerified(true);
        userRepository.save(user);

        return true;
    }

    @Override
    public void resendOtp(String email, OtpPurposes purpose, String ipAddress) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found"));

        verificationTokenRepository.findByUserAndPurpose(user, purpose)
                .ifPresent(verificationTokenRepository::delete);

        requestOtp(email, purpose, ipAddress);
    }

    // ================
    // Private Methods
    // ================

    private String generateOtpCode() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder otpCode = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            otpCode.append(secureRandom.nextInt(10)); // 0-9
        }

        return otpCode.toString();
    }

}
