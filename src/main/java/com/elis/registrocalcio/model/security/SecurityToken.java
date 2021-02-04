package com.elis.registrocalcio.model.security;

import com.elis.registrocalcio.dto.Token;
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
public class SecurityToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private Instant expirationDate;

    public SecurityToken() {
    }

    public SecurityToken(String username, Role role, Instant expirationDate) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.expirationDate = expirationDate;
    }

    public SecurityToken(Token token){
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public static Long getTokenId(Token token){
        String tokenIdAsString = token.token.split("-%\\$-")[0];
        return Long.parseLong(tokenIdAsString);
    }

    public static Instant getTokenExpirationDate(Token token){
        String expirationDateAsString = token.token.split("-%\\$-")[1];
        return Instant.parse(expirationDateAsString);
    }

    public static String getTokenIdAsString(Token token){
        return token.token.split("-%\\$-")[0];
    }
    public static String getTokenExpirationDateAsString(Token token){
        return token.token.split("-%\\$-")[1];
    }
}
