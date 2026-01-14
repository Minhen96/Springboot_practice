package com.example.mhpractice.features.user.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.common.exception.ErrorCode;
import com.example.mhpractice.features.notification.service.NotificationService;
import com.example.mhpractice.features.user.models.OtpResendTracking;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.models.VerificationToken;
import com.example.mhpractice.features.user.models.VerificationToken.OtpPurposes;
import com.example.mhpractice.features.user.repository.OtpResendTrackingRepository;
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

    private final OtpResendTrackingRepository otpResendTrackingRepository;

    private final NotificationService notificationService;

    @Override
    public void requestOtp(String email, OtpPurposes purpose, String ipAddress) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found"));

        // Generate OTP
        String otpCode = generateOtpCode();
        String hashedOtp = BCrypt.hashpw(otpCode, BCrypt.gensalt(10));

        // Create Verification Token (for verification)
        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .otpCode(hashedOtp)
                .purpose(purpose)
                .attemptCount(0)
                .maxAttempts(5)
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

        // Find Verification Token
        VerificationToken verificationToken = verificationTokenRepository.findByUserAndPurpose(user, purpose)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_NOT_FOUND, "OTP not found"));

        // Update Attempt Count
        verificationToken.setAttemptCount(verificationToken.getAttemptCount() + 1);

        // Check if max attempts exceeded
        if (verificationToken.getAttemptCount() > verificationToken.getMaxAttempts()) {
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }

        // Check if otp expired
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        // Check if otp already used
        if (verificationToken.isUsed()) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        // If otp is incorrect, update db and throw exception
        if (!BCrypt.checkpw(otpCode, verificationToken.getOtpCode())) {
            verificationTokenRepository.save(verificationToken);
            throw new BusinessException(ErrorCode.INVALID_OTP);
        }

        // If otp is correct, update db and return true
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        // Update user email verified
        user.setEmailVerified(true);
        userRepository.save(user);

        return true;
    }

    @Override
    public void resendOtp(String email, OtpPurposes purpose, String ipAddress) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found"));

        // Delete old verification token
        verificationTokenRepository.findByUserAndPurpose(user, purpose)
                .ifPresent(verificationTokenRepository::delete);

        // Find or create otp resend tracking
        OtpResendTracking otpResendTracking = otpResendTrackingRepository.findByEmailAndPurpose(email, purpose)
                .orElseGet(() -> OtpResendTracking.builder()
                        .email(email)
                        .purpose(purpose)
                        .lastResendAt(LocalDateTime.now())
                        .resetAt(LocalDateTime.now().plusMinutes(20))
                        .build());

        // Reset resend count if reset time is passed
        if (LocalDateTime.now().isAfter(otpResendTracking.getResetAt())) {
            otpResendTracking.setResendCount(0);
            otpResendTracking.setResetAt(LocalDateTime.now().plusMinutes(10));
        }

        // Check if resend interval is too short
        if (otpResendTracking.getLastResendAt() != null &&
                LocalDateTime.now().isBefore(
                        otpResendTracking.getLastResendAt().plusSeconds(otpResendTracking.getResendInterval()))) {
            throw new BusinessException(ErrorCode.OTP_REQUEST_TOO_FREQUENT);
        }

        // Check if max resend count exceeded
        if (otpResendTracking.getResendCount() >= otpResendTracking.getMaxResend()) {
            throw new BusinessException(ErrorCode.OTP_MAX_RESEND_EXCEEDED);
        }

        // Update resend count and last resend time
        otpResendTracking.setLastResendAt(LocalDateTime.now());
        otpResendTracking.setResendCount(otpResendTracking.getResendCount() + 1);
        otpResendTrackingRepository.save(otpResendTracking);

        // Request new otp
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
