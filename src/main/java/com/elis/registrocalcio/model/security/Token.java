package com.elis.registrocalcio.model.security;

import com.elis.registrocalcio.enumPackage.Role;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.Instant;

@Entity(name = "token")
public class Token implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false) //, columnDefinition = "varchar(20)'")
//    @Enumerated(EnumType.STRING)
    private String role;

    @Column(name = "expiration_date", nullable = false)
    private Instant expirationDate;

    public Token() {
    }

    public Token(String username, String role, Instant expirationDate) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.expirationDate = expirationDate;
    }


    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }
}
