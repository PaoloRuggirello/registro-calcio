package com.example.registrocalcio.repository;

import com.example.registrocalcio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {
    Long countByNameAndSurnameIgnoreCase(String name, String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

}
