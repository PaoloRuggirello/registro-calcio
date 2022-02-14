package com.elis.registrocalcio.handler;


import com.elis.registrocalcio.dto.UserDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.other.EmailServiceImpl;
import com.elis.registrocalcio.other.ExceptionUtils;
import com.elis.registrocalcio.other.PasswordHash;
import com.elis.registrocalcio.other.DateUtils;
import com.elis.registrocalcio.repository.general.UserRepository;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static com.elis.registrocalcio.enumPackage.FootballRegisterException.*;

@Service
public class UserHandler {

    @Autowired
    UserRepository userRepository;
    @Autowired
    EmailServiceImpl emailService;

    private static final Logger log = LogManager.getLogger(UserHandler.class);

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsernameAndIsActiveIsTrue(username);
    }

    public List<User> findActiveUsers(){
        return userRepository.findAllByIsActiveIsTrue();
    }

    public User findUserByUsernameCheckOptional(String username){
        Optional<User> userOptional = findUserByUsername(username);
        if(userOptional.isEmpty())
            ExceptionUtils.throwResponseStatus(this.getClass(), NOT_FOUND, USER_NOT_FOUND, "cannot find user: " + username);
        return userOptional.get();
    }


    /**
     * Create a user from UserDTO, save the user and remove password form userDTO to send it back
     * @param userToSave the user that the customer want to save
     * @return a user to send back to the front-end
     */
    public UserDTO createUserAndSave(UserDTO userToSave) throws InvalidKeySpecException, NoSuchAlgorithmException {
        setAvailableUsername(userToSave);
        userToSave.setPassword(passwordEncryption(userToSave.getPassword()));
        emailService.welcomeUser(save(new User(userToSave)));
        userToSave.setPassword(null);
        return userToSave;
    }

    /**
     * Check if fields used for login are empty or null
     * @param username - the user that should be logged in
     * @return a boolean value - true ok - false cannot do login
     */
    public boolean validateLoginFields(String username, String password){
        return validateUsername(username) && validatePassword(password);
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
                validateUsername(userToValidate.getUsername()) &&
                validatePassword(userToValidate.getPassword());
    }

    private boolean validateUsername(String username){
        return !StringUtils.isBlank(username);
    }

    public boolean validatePassword(String password){
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
        Matcher matcher = DateUtils.VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }

    /**
     * Check if the given mail is present in db
     * @param email of the user
     * @return a boolean value - true is present - false isn't present
     */
    public boolean checkIfPresentByEmail(String email){
        return userRepository.findByEmailAndIsActiveIsTrue(email).isPresent();
    }

    public String passwordEncryption(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        password = PasswordHash.createHash(password);
        return password;
    }

    /**
     * @param username and password of the user to authenticate
     * @return an empty user if the given user doesn't exist in db or has been passed wrong credentials, otherwise return the user
     */
    public Optional<User> checkUserCredentials(String username, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Optional<User> userOptional = userRepository.findByUsernameAndIsActiveIsTrue(username);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(PasswordHash.validatePassword(password, user.getPassword()))
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
        String username = toSetUsername.getUsername().split("\\[")[0];
        Long numberOfHomonyms = userRepository.countByNameAndSurnameIgnoreCaseAndIsActiveIsTrue(toSetUsername.getName(), toSetUsername.getSurname());
        log.warn("User with {} username already exist, assigned username = {}", username, username + numberOfHomonyms);
        if(numberOfHomonyms > 0)
            username += numberOfHomonyms;
        toSetUsername.setUsername(username);
        return toSetUsername;
    }

    public User changeUserRole(String username, String role){
        Optional<User> user = userRepository.findByUsernameAndIsActiveIsTrue(username);
        if(user.isPresent()){
            if(EnumUtils.isValidEnum(Role.class, role)){
                Role currentRole = user.get().getRole();
                user.get().setRole(Role.valueOf(role));
                log.info("Role for user {} changed from {} to {}", username, currentRole, user.get().getRole());
            }
            return userRepository.save(user.get());
        }
        ExceptionUtils.throwResponseStatus(this.getClass(), BAD_REQUEST, CANNOT_CHANGE_USER_ROLE , "user " + username +" not found");
        return null;
    }

    public void passwordRecoveryProcedure(User userToRecover) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String randomPass = randomPassword();
        userToRecover.setPassword(passwordEncryption(randomPass));
        userRepository.save(userToRecover);
        emailService.passwordRecovery(userToRecover.getName(), userToRecover.getEmail(), randomPass);
    }

    private String randomPassword(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


    public Long numberOfUsers(){
        return userRepository.count();
    }
}
