package com.elis.registrocalcio.other;

import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CleanTokenDB {

    @Autowired
    SecurityTokenRepository securityTokenRepository;

    @Scheduled(fixedRate = 900000)
    public void cleanTokenDB(){
        securityTokenRepository.deleteAllByExpirationDateIsBefore(Instant.now());
    }
}
