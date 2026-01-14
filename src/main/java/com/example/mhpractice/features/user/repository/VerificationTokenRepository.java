package com.example.mhpractice.features.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.models.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByUserAndPurpose(User user, VerificationToken.OtpPurposes purpose);
}
