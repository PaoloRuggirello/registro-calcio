package com.elis.registrocalcio.dto;

public class LoginDTO {
    public Token token;
    public String role;

    public LoginDTO() {
    }

    public LoginDTO(Token token, String role) {
        this.token = token;
        this.role = role;
    }
}
