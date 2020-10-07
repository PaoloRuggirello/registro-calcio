package com.example.registrocalcio.dto;

public class UserResponseDto {

    private String responseStatus;
    private UserDTO userDTO;

    public UserResponseDto() {
    }

    public UserResponseDto(String responseStatus, UserDTO userDTO) {
        this.responseStatus = responseStatus;
        this.userDTO = userDTO;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }
}
