package com.example.mhpractice.features.user.service;

import com.example.mhpractice.features.user.models.VerificationToken.OtpPurposes;

public interface OtpService {

    void requestOtp(String email, OtpPurposes purpose, String ipAddress);

    boolean verifyOtp(String email, OtpPurposes purpose, String otpCode, String ipAddress);

    void resendOtp(String email, OtpPurposes purpose, String ipAddress);

}
