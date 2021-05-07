package com.elis.registrocalcio.controller;

import com.elis.registrocalcio.dto.ChangePasswordDTO;
import com.elis.registrocalcio.dto.LoginDTO;
import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.handler.TokenHandler;
import com.elis.registrocalcio.handler.UserEventHandler;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.dto.UserDTO;
import com.elis.registrocalcio.dto.UserEventDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.EventHandler;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.general.UserEvent;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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


    @PostMapping("/authenticate")
    public LoginDTO authenticate(@RequestBody UserDTO userToAuthenticate) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if(!userHandler.validateLoginFields(userToAuthenticate.getUsername(), userToAuthenticate.getPassword()))// means that some fields are not ready for the login
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_LOGIN_FIELDS.toString());
        Optional<User> checkedUser = userHandler.checkUserCredentials(userToAuthenticate.getUsername(), userToAuthenticate.getPassword());
        System.out.println(checkedUser);
        return new LoginDTO(checkedUser.map(user -> tokenHandler.createToken(user)).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.AUTHENTICATION_FAILED.toString())), checkedUser.get().getRole().toString());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") Token userToken){
        tokenHandler.deleteToken(userToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userToRegister) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if(!userHandler.validateRegistrationFields(userToRegister))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_REGISTRATION_FIELDS.toString());
        if(userHandler.checkIfPresentByEmail(userToRegister.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.EMAIL_ALREADY_EXIST.toString());
        userHandler.createUserAndSave(userToRegister);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/bindWithEvent/{eventId}")
    public UserEventDTO bindUserAndEvent(@PathVariable("eventId") Long eventId, @RequestHeader("Authorization") Token userToken){
        String username = tokenHandler.checkToken(userToken).getUsername();
        User user = userHandler.findUserByUsernameCheckOptional(username);
        Event event = eventHandler.findEventByIdCheckOptional(eventId);
        Instant startOfFreePeriod = event.getDate().minus(event.getHourOfFreePeriod(), ChronoUnit.HOURS);
       if(userEventHandler.isAlreadyRegistered(user,event) && startOfFreePeriod.isAfter(Instant.now())) //User has active events and free period not started
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.CANNOT_REGISTER_USER.toString());
        UserEvent bound = new UserEvent(user, event);
        return new UserEventDTO(userEventHandler.save(bound));
    }

    @Transactional
    @PostMapping("/removeFromEvent/{eventId}")
    public ResponseEntity<String> removeBinding(@PathVariable("eventId") Long eventId, @RequestHeader("Authorization") Token userToken){
        String username = tokenHandler.checkToken(userToken).getUsername();
        User toRemoveBinding = userHandler.findUserByUsernameCheckOptional(username);
        Event event = eventHandler.findEventByIdCheckOptional(eventId);
        if(event.getPlayed() || Instant.now().plus(3, ChronoUnit.HOURS).isAfter(event.getDate())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, FootballRegisterException.CANNOT_REMOVE_BINDING.toString()); //Cannot remove binding if event is in less than 3 hours or played yet
        userEventHandler.deleteByUserAndEvent(toRemoveBinding, event);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/delete/{username}")
    public UserDTO deleteUser(@PathVariable("username") String username, @RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken, Role.ADMIN); //Users can only be deleted by admin
        User userToDelete = userHandler.findUserByUsernameCheckOptional(username);
        userEventHandler.deleteByUser(userToDelete);
        userToDelete.setActive(false);
        return new UserDTO(userHandler.save(userToDelete)).withoutPassword();
    }

    @GetMapping("/find")
    public List<UserDTO> findAll(@RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken, Role.ADMIN); //Only admin need all users
        return userHandler.findActiveUsers().stream().map(UserDTO::new).collect(Collectors.toList());
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

    @PostMapping("/changeRole/{username}")
    public UserDTO changeRole(@PathVariable("username") String username, @RequestHeader("Authorization") Token userToken){
        SecurityToken securityToken = tokenHandler.checkToken(userToken, Role.ADMIN);
        User updatedUser = userHandler.changeUserRole(username);
        if(username.equals(securityToken.getUsername())){
            securityToken.setRole(updatedUser.getRole());
            tokenHandler.save(securityToken);
        }
        return new UserDTO(updatedUser);
    }

    @PostMapping("/changeNewsletterStatus/")
    public ResponseEntity<String> changeNewsletter(@RequestHeader("Authorization") Token token){
        SecurityToken securityToken = tokenHandler.checkToken(token);
        User updateNewsLetter = userHandler.findUserByUsernameCheckOptional(securityToken.getUsername());
        updateNewsLetter.setNewsLetter(!updateNewsLetter.getNewsLetter()); //Changing newsLetter status
        userHandler.save(updateNewsLetter);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/passwordRecovery/{username}")
    public ResponseEntity<String> passwordRecovery(@PathVariable("username") String username) throws InvalidKeySpecException, NoSuchAlgorithmException {
        User user = userHandler.findUserByUsernameCheckOptional(username);
        userHandler.passwordRecoveryProcedure(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Optional<User> user = userHandler.checkUserCredentials(changePasswordDTO.username, changePasswordDTO.currentPassword);
        if(user.isEmpty() || !userHandler.validatePassword(changePasswordDTO.newPassword)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_LOGIN_FIELDS.toString()); //User not found or bad credentials
        user.get().setPassword(userHandler.passwordEncryption(changePasswordDTO.newPassword)); //Setting new password
        userHandler.save(user.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Autowired
    SecurityTokenRepository securityTokenRepository;
    @GetMapping("/findTokens")
    public List<SecurityToken> findTokens(){
        return securityTokenRepository.findAll();
    }
}
