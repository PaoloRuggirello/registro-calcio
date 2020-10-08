package com.example.registrocalcio.handler;


import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserHandler {

    @Autowired
    UserRepository userRepository;

    public Optional<User> checkPresentUser(UserDTO toAuthenticate){
        Optional<User> userOptional = userRepository.findByUsername(toAuthenticate.getUsername());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            //TODO: manage password encyption
            if(user.getPassword().equals(toAuthenticate.getPassword()))
                return userOptional;
        }
        return Optional.empty();
    }

    public String passwordEncryption(String password) {
        //TODO: Password encryption
        return password;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserDTO setAvailableUsername(UserDTO toSetUsername){
        int count = 1;
        String username = toSetUsername.getUsername();
        while(userRepository.countByUsername(username + count) > 0)
            count++;
        username += count;
        toSetUsername.setUsername(username);
        return toSetUsername;
    }
}
