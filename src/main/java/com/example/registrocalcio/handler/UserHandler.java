package com.example.registrocalcio.handler;


import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.other.PasswordHash;
import com.example.registrocalcio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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

    public String passwordEncryption(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        //TODO: Password encryption
        password = PasswordHash.createHash(password);
        return password;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * The usernames are set as name.surname, this method looks for user homonyms and set the username as the first available
     * @param toSetUsername the user that need an available username
     * @return the user with updated username
     */
    public UserDTO setAvailableUsername(UserDTO toSetUsername){
        String username = toSetUsername.getUsername();
        Long numberOfHomonyms = userRepository.countByNameAndSurnameIgnoreCase(toSetUsername.getName(), toSetUsername.getSurname());
        System.out.println("Number of homonymus : " + numberOfHomonyms);
        if(numberOfHomonyms > 0)
            username += numberOfHomonyms;
        toSetUsername.setUsername(username);
        return toSetUsername;
    }

    /**
     * Create a user from UserDTO, save the user and remove password form userDTO to send it back
     * @param userToSave the user that the customer want to save
     * @return a user to send back to the front-end
     */
    public UserDTO createUserAndSave(UserDTO userToSave) throws InvalidKeySpecException, NoSuchAlgorithmException {
        setAvailableUsername(userToSave);
        userToSave.setPassword(passwordEncryption(userToSave.getPassword()));
        saveUser(new User(userToSave));
        userToSave.setPassword(null);
        return userToSave;
    }
}
