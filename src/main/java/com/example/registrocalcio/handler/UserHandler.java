package com.example.registrocalcio.handler;


import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.enumPackage.FootballRegisterException;
import com.example.registrocalcio.enumPackage.Role;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.other.PasswordHash;
import com.example.registrocalcio.other.Utils;
import com.example.registrocalcio.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.regex.Matcher;

@Service
public class UserHandler {

    @Autowired
    UserRepository userRepository;



    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
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

    /**
     * Check if fields used for login are empty or null
     * @param userToValidate - the user that should be logged in
     * @return a boolean value - true ok - false cannot do login
     */
    public boolean validateLoginFields(UserDTO userToValidate){
        return validateUsername(userToValidate.getUsername()) && validatePassword(userToValidate.getPassword());
    }

    /**
     * Check if fields used for registration are present and usable
     * @param userToValidate - the user that should be registered
     * @return a boolean value - true ok - false cannot registrate the user
     */
    public boolean validateRegistrationFields(UserDTO userToValidate){
        return validateEmail(userToValidate.getEmail()) &&
                validateName(userToValidate.getName()) &&
                validateSurname(userToValidate.getSurname()) &&
                validateLoginFields(userToValidate);
    }

    private boolean validateUsername(String username){
        return !StringUtils.isBlank(username);
    }
    private boolean validatePassword(String password){
        return !(password == null);
    }
    private boolean validateName(String name){
        return !StringUtils.isBlank(name);
    }
    private boolean validateSurname(String surname){
        return !StringUtils.isBlank(surname);
    }
    private boolean validateEmail(String email){
        if(StringUtils.isBlank(email))
            return false;
        Matcher matcher = Utils.VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }

    /**
     * Check if the given mail is present in db
     * @param email of the user
     * @return a boolean value - true is present - false isn't present
     */
    public boolean checkIfPresentByEmail(String email){
        return userRepository.findByEmail(email).isPresent();
    }
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public String passwordEncryption(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        password = PasswordHash.createHash(password);
        return password;
    }

    /**
     * @param toAuthenticate - the user to authenticate
     * @return an empty user if the given user doesn't exist in db or has been passed wrong credentials, otherwise return the user
     */
    public Optional<User> checkUserCredentials(UserDTO toAuthenticate) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Optional<User> userOptional = userRepository.findByUsername(toAuthenticate.getUsername());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(PasswordHash.validatePassword(toAuthenticate.getPassword(), user.getPassword()))
//            if(user.getPassword().equals(toAuthenticate.getPassword()))
                return userOptional;
        }
        return Optional.empty();
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

    public boolean hasUserPermissions(Role permissionLevel, String username) {
        Optional<String> roleOptional = userRepository.findRoleByUsername(username);
        if(roleOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FootballRegisterException.USER_NOT_FOUND.toString());
        String role = roleOptional.get();
        if(role.equals(Role.SUPER_ADMIN.toString()))
            return true;
        if(role.equals(Role.ADMIN.toString()) && (permissionLevel == Role.ADMIN || permissionLevel == Role.USER))
            return true;
        if(role.equals(Role.USER.toString()) && permissionLevel == Role.USER)
            return true;
        return false;
    }
}
