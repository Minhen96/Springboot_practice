package com.example.mhpractice.features.notification.service;

import java.math.BigDecimal;

import com.example.mhpractice.features.notification.model.Notification;
import com.example.mhpractice.features.notification.service.request.SendMailRequest;

public interface NotificationService {
    public void sendTextEmail(SendMailRequest sendMailRequest);

    public void sendWelcomeEmail(String to);

    public void sendOtpEmail(String to, String otpCode);

    public void sendTransactionSuccessEmail(String userEmail, String transactionId, BigDecimal amount);

    public void sendTransactionFailedEmail(String userEmail, String transactionId, String reason);

    public void saveNotification(Notification notification);
}
