package com.example.registrocalcio.handler;


import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class UserHandler {

    @Autowired
    UserRepository userRepository;

    public boolean checkCredentials(UserDTO toAuthenticate){
        Optional<User> userOptional = userRepository.findByUsername(toAuthenticate.getUsername());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            //TODO: manage password encyption
            if(user.getPassword().equals(toAuthenticate.getPassword()))
                return true;
        }
        //TODO: manage not found user
        return false;
    }
}
