package com.example.registrocalcio.repository;

import com.example.registrocalcio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {

    Long countByUsername(String username);
    Optional<User> findByUsername(String username);
}
