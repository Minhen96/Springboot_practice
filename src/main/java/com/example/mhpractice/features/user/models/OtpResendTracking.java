package com.example.mhpractice.features.user.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.example.mhpractice.features.user.models.VerificationToken.OtpPurposes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "otp_resend_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResendTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "purpose", nullable = false)
    private OtpPurposes purpose;

    @Column(name = "resend_count", nullable = false)
    @Builder.Default
    private int resendCount = 0;

    @Column(name = "max_resend", nullable = false)
    @Builder.Default
    private int maxResend = 5;

    // set resendInterval to 60 seconds
    @Column(name = "resend_interval", nullable = false)
    @Builder.Default
    private int resendInterval = 60;

    @Column(name = "last_resend_at")
    private LocalDateTime lastResendAt;

    // set resetAt to current time + 10 minutes
    @Column(nullable = false)
    private LocalDateTime resetAt;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
