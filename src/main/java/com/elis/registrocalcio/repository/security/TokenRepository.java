package com.elis.registrocalcio.repository.security;

import com.elis.registrocalcio.model.security.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository <Token, Long> {
}
