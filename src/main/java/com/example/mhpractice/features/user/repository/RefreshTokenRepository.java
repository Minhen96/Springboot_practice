package com.example.mhpractice.features.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mhpractice.features.user.models.RefreshToken;
import com.example.mhpractice.features.user.models.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
