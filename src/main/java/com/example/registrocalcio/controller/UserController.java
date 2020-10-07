package com.example.registrocalcio.controller;

import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.dto.UserResponseDto;
import com.example.registrocalcio.handler.UserHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {

    @Autowired
    private UserHandler userHandler;

    @PostMapping
    public UserResponseDto authenticate(@RequestBody UserDTO toAuthenticate){
        UserResponseDto response = new UserResponseDto("Not Found", null);
        if(userHandler.checkCredentials(toAuthenticate)) {
            response.setResponseStatus("OK");
            response.setUserDTO(toAuthenticate);
        }else {
            response.setResponseStatus("Denied");
        }
        return response;
    }



}
