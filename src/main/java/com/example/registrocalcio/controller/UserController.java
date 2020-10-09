package com.example.registrocalcio.controller;

import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.handler.UserHandler;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.other.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserHandler userHandler;

    @PostMapping("/authenticate")
    public UserDTO authenticate(@RequestBody UserDTO toAuthenticate){
        System.out.println(toAuthenticate);
        Optional<User> checkedUser = userHandler.checkPresentUser(toAuthenticate);
        return checkedUser.map(UserDTO::new).orElse(null);
    }

    @PostMapping("/register")
    public UserDTO registerUser(@RequestBody UserDTO toRegister) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return userHandler.createUserAndSave(toRegister);
    }



}
