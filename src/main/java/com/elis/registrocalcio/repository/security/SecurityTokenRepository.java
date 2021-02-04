package com.elis.registrocalcio.repository.security;

import com.elis.registrocalcio.model.security.SecurityToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityTokenRepository extends JpaRepository <SecurityToken, Long> {

    Optional<SecurityToken> findByUsername(String username);

    void deleteById(Long id);
}
