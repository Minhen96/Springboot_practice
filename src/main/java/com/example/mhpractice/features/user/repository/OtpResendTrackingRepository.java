package com.example.mhpractice.features.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mhpractice.features.user.models.OtpResendTracking;
import com.example.mhpractice.features.user.models.VerificationToken.OtpPurposes;

public interface OtpResendTrackingRepository extends JpaRepository<OtpResendTracking, UUID> {

    Optional<OtpResendTracking> findByEmailAndPurpose(String email, OtpPurposes purpose);

    void deleteAllByResetAtBefore(LocalDateTime dateTime);

}
