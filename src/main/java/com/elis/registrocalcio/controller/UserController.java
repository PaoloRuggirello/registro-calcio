package com.elis.registrocalcio.controller;

import com.elis.registrocalcio.dto.ChangePasswordDTO;
import com.elis.registrocalcio.dto.LoginDTO;
import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.handler.TokenHandler;
import com.elis.registrocalcio.handler.UserEventHandler;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.dto.UserDTO;
import com.elis.registrocalcio.dto.UserEventDTO;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.EventHandler;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.general.UserEvent;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.other.ExceptionUtils;
import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elis.registrocalcio.enumPackage.FootballRegisterException.OVERLAPPING_EVENTS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static com.elis.registrocalcio.enumPackage.FootballRegisterException.AUTHENTICATION_FAILED;
import static com.elis.registrocalcio.enumPackage.FootballRegisterException.INVALID_LOGIN_FIELDS;
import static com.elis.registrocalcio.enumPackage.FootballRegisterException.INVALID_REGISTRATION_FIELDS;
import static com.elis.registrocalcio.enumPackage.FootballRegisterException.EMAIL_ALREADY_EXIST;
import static com.elis.registrocalcio.enumPackage.FootballRegisterException.CANNOT_REGISTER_USER;
import static com.elis.registrocalcio.enumPackage.FootballRegisterException.PERMISSION_DENIED;
import static com.elis.registrocalcio.enumPackage.FootballRegisterException.CANNOT_REMOVE_BINDING;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserHandler userHandler;
    @Autowired
    private EventHandler eventHandler;
    @Autowired
    private UserEventHandler userEventHandler;
    @Autowired
    private TokenHandler tokenHandler;

    private static Logger log = LogManager.getLogger(UserController.class);

    @PostMapping("/authenticate")
    public LoginDTO authenticate(@RequestBody UserDTO userToAuthenticate) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if(!userHandler.validateLoginFields(userToAuthenticate.getUsername(), userToAuthenticate.getPassword()))// means that some fields are not ready for the login
            ExceptionUtils.throwResponseStatus(this.getClass(), BAD_REQUEST, INVALID_LOGIN_FIELDS);
        Optional<User> checkedUser = userHandler.checkUserCredentials(userToAuthenticate.getUsername(), userToAuthenticate.getPassword());
        log.info("User {} -> {}", userToAuthenticate.getUsername(), checkedUser.isPresent() ? "login ok" : "login failed");
        return new LoginDTO(checkedUser.map(user -> tokenHandler.createToken(user.getUsername())).orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, AUTHENTICATION_FAILED.toString())), checkedUser.get().getRole().toString());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") Token userToken){
        tokenHandler.deleteToken(userToken);
        return new ResponseEntity<>(OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userToRegister) throws InvalidKeySpecException, NoSuchAlgorithmException {
        log.info("Registration: {}", userToRegister);
        if(!userHandler.validateRegistrationFields(userToRegister))
            ExceptionUtils.throwResponseStatus(this.getClass(), BAD_REQUEST, INVALID_REGISTRATION_FIELDS);
        if(userHandler.checkIfPresentByEmail(userToRegister.getEmail()))
            ExceptionUtils.throwResponseStatus(this.getClass(), BAD_REQUEST, EMAIL_ALREADY_EXIST);
        if(userHandler.numberOfUsers() == 0){
            userToRegister.role = Role.ADMIN.name();
        }
        userHandler.createUserAndSave(userToRegister);
        return new ResponseEntity<>(OK);
    }

    @Transactional
    @PostMapping("/bindWithEvent/{eventId}")
    public UserEventDTO bindUserAndEvent(@PathVariable("eventId") Long eventId, @RequestHeader("Authorization") Token userToken){
        String username = tokenHandler.checkToken(userToken).getUsername();
        log.info("Binding user {} with event with id {}", username, eventId);
        log.info("UserToken: {}", userToken);
        User user = userHandler.findUserByUsernameCheckOptional(username);
        Event event = eventHandler.findEventByIdCheckOptional(eventId);
        if(!userEventHandler.freeCategories.contains(event.getCategory())) {
            Instant startOfFreePeriod = event.getDate().minus(event.getHourOfFreePeriod(), ChronoUnit.HOURS);
            if (userEventHandler.isAlreadyRegistered(user, event) || (userEventHandler.hasActiveEvents(user)) && startOfFreePeriod.isAfter(Instant.now())) //User has active events and free period not started
                ExceptionUtils.throwResponseStatus(this.getClass(), BAD_REQUEST, CANNOT_REGISTER_USER);
        }
        if(userEventHandler.hasOverlappingEvents(user, event)){
            ExceptionUtils.throwResponseStatus(this.getClass(), BAD_REQUEST, OVERLAPPING_EVENTS);
        }
        UserEvent bound = new UserEvent(user, event);
        return new UserEventDTO(userEventHandler.save(bound));
    }

    @Transactional
    @PostMapping("/removeFromEvent/{eventId}/{username}")
    public ResponseEntity<String> removeBinding(@PathVariable("eventId") Long eventId, @PathVariable("username") String username, @RequestHeader("Authorization") Token userToken){
        SecurityToken token = tokenHandler.checkToken(userToken);
        log.info("{} is removing {} from event {}", token.getUsername(), username, eventId);
        if(token.getRole().equals(Role.USER) && !token.getUsername().equals(username)) { //If a user is trying to delete another user from the match
            ExceptionUtils.throwResponseStatus(this.getClass(), FORBIDDEN, PERMISSION_DENIED);
        }
        User toRemoveBinding = userHandler.findUserByUsernameCheckOptional(username);
        Event event = eventHandler.findEventByIdCheckOptional(eventId);
        if(event.getPlayed() || (token.getRole().equals(Role.USER) && Instant.now().plus(event.getHourOfNoDeleteZone(), ChronoUnit.HOURS).isAfter(event.getDate()))) ExceptionUtils.throwResponseStatus(this.getClass(), FORBIDDEN, CANNOT_REMOVE_BINDING); //Cannot remove binding if event is in less than 3 hours or played yet
        userEventHandler.deleteByUserAndEvent(toRemoveBinding, event);
        User appointed = toRemoveBinding;
        if(!token.getUsername().equals(username))
            appointed = userHandler.findUserByUsername(token.getUsername()).get();
        eventHandler.communicateRemoval(appointed.getName() + " " + appointed.getSurname(), toRemoveBinding.getEmail(), event);
        return new ResponseEntity<>(OK);
    }

    @Transactional
    @PostMapping("/delete/{username}")
    public UserDTO deleteUser(@PathVariable("username") String username, @RequestHeader("Authorization") Token userToken){
        String tokenUsername = tokenHandler.checkToken(userToken, Role.ADMIN).getUsername(); //Users can only be deleted by admin
        log.info("{} is removing user: {}", tokenUsername, username);
        User userToDelete = userHandler.findUserByUsernameCheckOptional(username);
        userEventHandler.deleteByUser(userToDelete);
        userToDelete.setActive(false);
        log.info("{} removed", username);
        if(tokenUsername.equals(username)) logout(tokenHandler.createToken(tokenUsername));
        return new UserDTO(userHandler.save(userToDelete)).withoutPassword();
    }

    @GetMapping("/find")
    public List<UserDTO> findAll(@RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken, Role.ADMIN); //Only admins need all users
        List<User> allUsers = userHandler.findActiveUsers();
        return allUsers.stream().map(UserDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/findInfo")
    public UserDTO findUserInfo(@RequestHeader("Authorization") Token userToken){
        SecurityToken token = tokenHandler.checkToken(userToken);
        return new UserDTO(userHandler.findUserByUsernameCheckOptional(token.getUsername()));
    }

    @GetMapping("/findSubscribed")
    public List<EventDTO> findSubscribed(@RequestHeader("Authorization") Token userToken){
        String username = tokenHandler.checkToken(userToken).getUsername();
        return userEventHandler.findEventsSubscribedByUser(username).stream().map(EventDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/credits")
    public String credits(){
        return "Created By Alessio Billeci and Paolo Ruggirello";
    }

    @PostMapping("/changeRole/{username}/{role}")
    public UserDTO changeRole(@PathVariable("username") String username, @PathVariable("role") String role, @RequestHeader("Authorization") Token userToken){
        SecurityToken securityToken = tokenHandler.checkToken(userToken, Role.ADMIN);
        log.info("{} is changing role for user {}", securityToken.getUsername(), username);
        User updatedUser = userHandler.changeUserRole(username, role);
        if(username.equals(securityToken.getUsername())){
            securityToken.setRole(updatedUser.getRole());
            tokenHandler.save(securityToken);
            logout(tokenHandler.createToken(securityToken.getUsername()));//If user is changing his role -> logout
        }
        return new UserDTO(updatedUser);
    }

    @PostMapping("/changeNewsletterStatus/")
    public ResponseEntity<String> changeNewsletter(@RequestHeader("Authorization") Token token){
        SecurityToken securityToken = tokenHandler.checkToken(token);
        User updateNewsLetter = userHandler.findUserByUsernameCheckOptional(securityToken.getUsername());
        log.info("{} is changing his newsletter from {} to {}", securityToken.getUsername(), updateNewsLetter.getNewsLetter(), !updateNewsLetter.getNewsLetter());
        updateNewsLetter.setNewsLetter(!updateNewsLetter.getNewsLetter()); //Changing newsLetter status
        userHandler.save(updateNewsLetter);
        return new ResponseEntity<>(OK);
    }

    @PostMapping("/passwordRecovery/{username}")
    public ResponseEntity<String> passwordRecovery(@PathVariable("username") String username) throws InvalidKeySpecException, NoSuchAlgorithmException {
        log.info("recover password procedure started for user {}", username);
        User user = userHandler.findUserByUsernameCheckOptional(username);
        userHandler.passwordRecoveryProcedure(user);
        return new ResponseEntity<>(OK);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Optional<User> user = userHandler.checkUserCredentials(changePasswordDTO.username, changePasswordDTO.currentPassword);
        if(user.isEmpty() || !userHandler.validatePassword(changePasswordDTO.newPassword)) ExceptionUtils.throwResponseStatus(this.getClass(), BAD_REQUEST, INVALID_LOGIN_FIELDS); //User not found or bad credentials
        user.get().setPassword(userHandler.passwordEncryption(changePasswordDTO.newPassword)); //Setting new password
        userHandler.save(user.get());
        return new ResponseEntity<>(OK);
    }

    @Autowired
    SecurityTokenRepository securityTokenRepository;
    @GetMapping("/findTokens")
    public List<SecurityToken> findTokens(){
        return securityTokenRepository.findAll();
    }
}
