package com.elis.registrocalcio.controller;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
    public Token authenticate(@RequestBody UserDTO userToAuthenticate) throws InvalidKeySpecException, NoSuchAlgorithmException {
        System.out.println(userToAuthenticate);
        if(!userHandler.validateLoginFields(userToAuthenticate))// means that some fields are not ready for the login
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_LOGIN_FIELDS.toString());
        Optional<User> checkedUser = userHandler.checkUserCredentials(userToAuthenticate);
        System.out.println(checkedUser);
        if(checkedUser.isPresent())
            return tokenHandler.createToken(checkedUser.get());
//        return checkedUser.map(UserDTO::new).orElse(null);
        return null;
    }

    @PostMapping("/logout")
    public String logout(@RequestBody Token userToken){
        tokenHandler.deleteToken(userToken);
        return "Successfully deleted token";
    }

    @PostMapping("/register")
    public Token registerUser(@RequestBody UserDTO userToRegister) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if(!userHandler.validateRegistrationFields(userToRegister))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_REGISTRATION_FIELDS.toString());
        if(userHandler.checkIfPresentByEmail(userToRegister.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.EMAIL_ALREADY_EXIST.toString());
        userHandler.createUserAndSave(userToRegister);
        return tokenHandler.createToken(new User(userToRegister));
    }

    @PostMapping("/bindWithEvent")
    public UserEventDTO bindUserAndEvent(@RequestBody UserEventDTO toBind, @RequestBody Token userToken){
        tokenHandler.checkIfAreTheSameUser(userToken, toBind.getPlayerUsername());
        User user = userHandler.findUserByUsernameCheckOptional(toBind.getPlayerUsername());
        Event event = eventHandler.findEventByIdCheckOptional(toBind.getEventId());
        if(event.getDate().plus(-3, ChronoUnit.HOURS).isBefore(new Date().toInstant()) || userEventHandler.isAlreadyRegistered(user,event)) // if there is less than 3 hours to the event or if the user is already registered to a valid event
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.CANNOT_REGISTER_USER.toString());
        UserEvent bound = new UserEvent(user, event, toBind);
        return new UserEventDTO(userEventHandler.save(bound));
    }

    @Transactional
    @PostMapping("/removeFromEvent/{username}/{eventId}")
    public void removeBinding(@PathVariable("username")String username, @PathVariable("eventId") Long eventId, @RequestBody Token userToken){
        tokenHandler.checkIfAreTheSameUser(userToken, username);
        User toRemoveBinding = userHandler.findUserByUsernameCheckOptional(username);
        Event event = eventHandler.findEventByIdCheckOptional(eventId);
        userEventHandler.deleteByUserAndEvent(toRemoveBinding, event);
    }

    @Transactional
    @PostMapping("/delete/{username}")
    public UserDTO deleteUser(@PathVariable("username") String username, @RequestBody Token userToken){
        tokenHandler.checkToken(userToken, Role.ADMIN); //Users can only be deleted by admin
        User userToDelete = userHandler.findUserByUsernameCheckOptional(username);
        userEventHandler.deleteByUser(userToDelete);
        userToDelete.setActive(false);
        return new UserDTO(userHandler.save(userToDelete)).withoutPassword();
    }

    @GetMapping("/find")
    public List<UserDTO> findAll(@RequestBody Token userToken){
        tokenHandler.checkToken(userToken, Role.ADMIN); //Only admin need all users
        return userHandler.findActiveUsers().stream().map(UserDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/find/{username}")
    public UserDTO findUser(@PathVariable("username")String username, @RequestBody Token userToken){
        tokenHandler.checkToken(userToken);
        return new UserDTO(userHandler.findUserByUsernameCheckOptional(username));
    }

    @GetMapping("/findBoundEvents/{username}")
    public List<EventDTO> findBoundEvents(@PathVariable("username")String username, @RequestBody Token userToken){
        tokenHandler.checkIfAreTheSameUser(userToken, username);
        return userEventHandler.findByUser(userHandler.findUserByUsernameCheckOptional(username)).stream().map(EventDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/credits")
    public String credits(){
        return "Created By Alessio Billeci and Paolo Ruggirello";
    }


    @Autowired
    SecurityTokenRepository securityTokenRepository;
    @GetMapping("/findTokens")
    public List<SecurityToken> findTokens(){
        return securityTokenRepository.findAll();
    }

}
