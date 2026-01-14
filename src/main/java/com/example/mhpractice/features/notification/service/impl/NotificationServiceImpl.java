package com.example.mhpractice.features.notification.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.common.exception.ErrorCode;
import com.example.mhpractice.features.notification.model.Notification;
import com.example.mhpractice.features.notification.service.NotificationService;
import com.example.mhpractice.features.notification.service.request.SendMailRequest;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromAddress;

    @Async
    @Override
    public void sendTextEmail(SendMailRequest sendMailRequest) {
        try {
            // Create a mime message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Create Helper (2nd param: multipart support, 3rd: encoding)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set the cc and bcc if needed
            // helper.setCc("manager@example.com");
            // helper.setBcc("admin@example.com");

            // Set the from, to, and subject
            helper.setFrom(fromAddress);
            helper.setTo(sendMailRequest.getTo());
            helper.setSubject(sendMailRequest.getSubject());

            // Set the body
            helper.setText(sendMailRequest.getBody(), true);

            // Send the email
            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EMAIL_SERVER_ERROR, "Failed to send email");
        }
    }

    @Override
    public void sendWelcomeEmail(String to) {
        try {
            // Create a mime message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Create Helper (2nd param: multipart support, 3rd: encoding)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set the cc and bcc if needed
            // helper.setCc("manager@example.com");
            // helper.setBcc("admin@example.com");

            // Set the from, to, and subject
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Welcome to Mini TNG");

            // Set the context for the thymeleaf template
            Context context = new Context();
            // context.setVariable("userName", userName);

            // Process template into HTML string
            String htmlContent = templateEngine.process("email/welcome-email", context);

            // Set the body (the 2nd param true, mean is html)
            helper.setText(htmlContent, true);

            // Send the email
            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EMAIL_SERVER_ERROR, "Failed to send email");
        }
    }

    @Override
    public void sendOtpEmail(String to, String otpCode) {
        try {
            // Create a mime message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Create Helper (2nd param: multipart support, 3rd: encoding)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set the from, to, and subject
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Mini Tng OTP Verification");

            // Set the body (the 2nd param true, mean is html)
            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            String htmlContent = templateEngine.process("email/verification-email-otp", context);
            helper.setText(htmlContent, true);

            // Send the email
            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EMAIL_SERVER_ERROR, "Failed to send email");
        }
    }

    @Override
    public void sendTransactionSuccessEmail(String userEmail, String transactionId, BigDecimal amount) {

    }

    @Override
    public void sendTransactionFailedEmail(String userEmail, String transactionId, String reason) {

    }

    @Override
    public void saveNotification(Notification notification) {

    }
}
