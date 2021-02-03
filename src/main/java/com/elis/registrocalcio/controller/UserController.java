package com.elis.registrocalcio.controller;

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
import com.elis.registrocalcio.model.security.Token;
import com.elis.registrocalcio.repository.general.UserRepository;
import com.elis.registrocalcio.repository.security.TokenRepository;
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
import java.time.Instant;
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

    @PostMapping("/bindWithEvent")
    public UserEventDTO bindUserAndEvent(@RequestBody UserEventDTO toBind){
        User user = userHandler.findUserByUsernameCheckOptional(toBind.getPlayerUsername());
        Event event = eventHandler.findEventByIdCheckOptional(toBind.getEventId());
        if(event.getDate().plus(-3, ChronoUnit.HOURS).isBefore(new Date().toInstant()) || userEventHandler.isAlreadyRegistered(user,event)) // if there is less than 3 hours to the event or if the user is already registered to a valid event
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.CANNOT_REGISTER_USER.toString());
        UserEvent bound = new UserEvent(user, event, toBind);
        return new UserEventDTO(userEventHandler.save(bound));
    }

    @Transactional
    @PostMapping("/removeFromEvent/{username}/{eventId}")
    public void removeBinding(@PathVariable("username")String username, @PathVariable("eventId") Long eventId,  @RequestBody UserDTO inCharge){
        User employee = userHandler.findUserByUsernameCheckOptional(inCharge.getUsername());
        userHandler.hasUserPermissions(Role.ADMIN, employee.getRole());
        User toRemoveBinding = userHandler.findUserByUsernameCheckOptional(username);
        Event event = eventHandler.findEventByIdCheckOptional(eventId);
        userEventHandler.deleteByUserAndEvent(toRemoveBinding, event);
    }

    @Transactional
    @PostMapping("/delete/{username}")
    public UserDTO deleteUser(@PathVariable("username") String username, @RequestBody UserDTO inCharge){
        User employee = userHandler.findUserByUsernameCheckOptional(inCharge.getUsername());
        userHandler.hasUserPermissions(Role.ADMIN, employee.getRole());
        User userToDelete = userHandler.findUserByUsernameCheckOptional(username);
        userEventHandler.deleteByUser(userToDelete);
        userToDelete.setActive(false);
        return new UserDTO(userHandler.save(userToDelete)).withoutPassword();
    }

    @GetMapping("/find")
    public List<UserDTO> findAll(){
        return userHandler.findActiveUsers().stream().map(UserDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/find/{username}")
    public UserDTO findUser(@PathVariable("username")String username){
        return new UserDTO(userHandler.findUserByUsernameCheckOptional(username));
    }

    @GetMapping("/findBoundEvents/{username}")
    public List<EventDTO> findBoundEvents(@PathVariable("username")String username){
        return userEventHandler.findByUser(userHandler.findUserByUsernameCheckOptional(username)).stream().map(EventDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/credits")
    public String credits(){
        return "Created By Alessio Billeci and Paolo Ruggirello";
    }

    @Autowired
    TokenRepository tokenRepository;

    @GetMapping("/createToken")
    public void createToken(){
        Token token = new Token("paolo.ruggirello", Role.ADMIN.toString(), Instant.now());
        tokenRepository.save(token);
    }

    @GetMapping("/findToken")
    public List<Token> findToken(){
        return tokenRepository.findAll();
    }

    @Autowired
    UserRepository userRepository;

    @GetMapping("/createUser")
    public void createUser(){
        User paolo = new User("paolo.ruggirello", "paolo", "ruggirello", "ruggirello99@live.it", "password");
        userRepository.save(paolo);
    }

    @GetMapping("/findUser")
    public List<User> findUser(){
        return userRepository.findAll();
    }


}
