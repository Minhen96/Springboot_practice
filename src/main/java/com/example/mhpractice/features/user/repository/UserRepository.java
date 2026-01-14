package com.example.mhpractice.features.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.mhpractice.features.user.models.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

    boolean existsByEmail(String email);

}
