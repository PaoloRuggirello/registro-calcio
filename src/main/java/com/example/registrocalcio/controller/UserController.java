package com.example.registrocalcio.controller;

import com.example.registrocalcio.dto.EventDTO;
import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.dto.UserEventDTO;
import com.example.registrocalcio.enumPackage.FootballRegisterException;
import com.example.registrocalcio.enumPackage.Role;
import com.example.registrocalcio.handler.EventHandler;
import com.example.registrocalcio.handler.UserEventHandler;
import com.example.registrocalcio.handler.UserHandler;
import com.example.registrocalcio.model.Event;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.model.UserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class UserController {

    @Autowired
    private UserHandler userHandler;
    @Autowired
    private EventHandler eventHandler;
    @Autowired
    private UserEventHandler userEventHandler;


    @PostMapping("/authenticate")
    public UserDTO authenticate(@RequestBody UserDTO userToAuthenticate) throws InvalidKeySpecException, NoSuchAlgorithmException {
        System.out.println("I'm here");
        System.out.println(userToAuthenticate);
        if(!userHandler.validateLoginFields(userToAuthenticate))// means that some fields are not ready for the login
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.INVALID_LOGIN_FIELDS.toString());
        Optional<User> checkedUser = userHandler.checkUserCredentials(userToAuthenticate);
        System.out.println(checkedUser);
        return checkedUser.map(UserDTO::new).orElse(null);
    }
    @PostMapping("/logout")
    public void logout(){
        System.out.println("User logged out");
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

    @GetMapping("/userr")
    public String credits(){
        return "Created By Alessio Billeci and Paolo Ruggirello";
    }

    @GetMapping("/test/{password}")
    public String credits(@PathVariable("password") String password){
        return userHandler.passwordEncryption(password);
    }

    @GetMapping("/user")
    public String getUser(Principal principal){
        return principal.toString();
    }


}
