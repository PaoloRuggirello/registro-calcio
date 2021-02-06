package com.elis.registrocalcio.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangePasswordDTO {
    public String username;
    public String currentPassword;
    public String newPassword;

    public ChangePasswordDTO() {
    }
}
