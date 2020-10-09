package com.example.registrocalcio.controller;

import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.enumPackage.FootballRegisterException;
import com.example.registrocalcio.handler.UserHandler;
import com.example.registrocalcio.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public UserDTO authenticate(@RequestBody UserDTO userToAuthenticate) throws InvalidKeySpecException, NoSuchAlgorithmException {
        System.out.println(userToAuthenticate);
        if(!userHandler.validateLoginFields(userToAuthenticate))// means that some fields are not ready for the login
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_LOGIN_FIELDS.toString());
        Optional<User> checkedUser = userHandler.checkUserCredentials(userToAuthenticate);
        System.out.println(checkedUser);
        return checkedUser.map(UserDTO::new).orElse(null);
    }

    @PostMapping("/register")
    public UserDTO registerUser(@RequestBody UserDTO userToRegister) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if(!userHandler.validateRegistrationFields(userToRegister))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_REGISTRATION_FIELDS.toString());
        if(userHandler.checkIfPresentByEmail(userToRegister.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.EMAIL_ALREADY_EXIST.toString());
        return userHandler.createUserAndSave(userToRegister);
    }



}
