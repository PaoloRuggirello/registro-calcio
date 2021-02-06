package com.elis.registrocalcio;

import com.elis.registrocalcio.controller.UserController;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.dto.UserDTO;
import com.elis.registrocalcio.dto.UserEventDTO;
import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.TokenHandler;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.general.UserEvent;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.repository.general.EventRepository;
import com.elis.registrocalcio.repository.general.UserEventRepository;
import com.elis.registrocalcio.repository.general.UserRepository;
import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static com.elis.registrocalcio.model.security.SecurityToken.getTokenId;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.*;

@SpringBootTest
public class UserControllerTest {
    @Autowired
    UserController userController;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserHandler userHandler;
    @Autowired
    TokenHandler tokenHandler;
    @Autowired
    SecurityTokenRepository securityTokenRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    UserEventRepository userEventRepository;

    @BeforeEach
    public void dropDBs(){
        securityTokenRepository.deleteAll();
        userEventRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testAuthentication() throws InvalidKeySpecException, NoSuchAlgorithmException {
        //Check Auth ok
        User userToAuthenticate = new User("user.user", "user", "user", "user@email.it", userHandler.passwordEncryption("password"));
        userToAuthenticate = userRepository.save(userToAuthenticate);
        UserDTO toAuthenticate = new UserDTO(userToAuthenticate.getUsername(), "password");
        assertNotNull(userController.authenticate(toAuthenticate));

        //Check username empty
        UserDTO wrong = new UserDTO("", userHandler.passwordEncryption("password"));
        Throwable testException = assertThrows(ResponseStatusException.class, () -> userController.authenticate(wrong), FootballRegisterException.INVALID_LOGIN_FIELDS.toString());
        assertThat(testException.getMessage(), containsString(FootballRegisterException.INVALID_LOGIN_FIELDS.toString()));


        //Check password null
        wrong.setUsername("username");
        wrong.setPassword(null);
        testException = assertThrows(ResponseStatusException.class, () -> userController.authenticate(wrong), FootballRegisterException.INVALID_LOGIN_FIELDS.toString());
        assertThat(testException.getMessage(), containsString(FootballRegisterException.INVALID_LOGIN_FIELDS.toString()));


        //Check authentication fail, invalid username or password
        wrong.setPassword(userHandler.passwordEncryption("password"));
        testException = assertThrows(ResponseStatusException.class, () -> userController.authenticate(wrong), FootballRegisterException.INVALID_LOGIN_FIELDS.toString());
        assertThat(testException.getMessage(), containsString(FootballRegisterException.AUTHENTICATION_FAILED.toString()));
    }

    @Test
    public void testLogout() throws InvalidKeySpecException, NoSuchAlgorithmException {
        User testUser = new User("username", "name", "surname", "emailo@email.it", userHandler.passwordEncryption("password"));
        testUser.setRole(Role.ADMIN);
        userRepository.save(testUser);
        Token userToken = tokenHandler.createToken(testUser);

        //Check if token is present
        userToken.decrypt();
        Optional<SecurityToken> savedToken = securityTokenRepository.findById(getTokenId(userToken));
        assertTrue(savedToken.isPresent());

        //Check if Token works
        userToken.encrypt();
        assertNotNull(userController.findAll(userToken));

        //logout
        userToken.encrypt();
        userController.logout(userToken);

        //Check token in db
        savedToken = securityTokenRepository.findById(getTokenId(userToken));
        assertFalse(savedToken.isPresent());

        //Check that token doesn't works
        userToken.encrypt();
        Throwable testException = assertThrows(ResponseStatusException.class, () -> userController.findAll(userToken), FootballRegisterException.INVALID_TOKEN.toString());
        assertThat(testException.getMessage(), containsString(FootballRegisterException.INVALID_TOKEN.toString()));


        //CheckPermissionLevel
        testUser.setRole(Role.USER);
        userRepository.save(testUser);
        Token newUserToken = tokenHandler.createToken(testUser);
        testException = assertThrows(ResponseStatusException.class, () -> userController.findAll(newUserToken), FootballRegisterException.PERMISSION_DENIED.toString());
        assertThat(testException.getMessage(), containsString(FootballRegisterException.PERMISSION_DENIED.toString()));

    }

    @Test
    public void testRegistration() throws InvalidKeySpecException, NoSuchAlgorithmException {
        //Try register user with wrong email
        User wrong = new User("username", "name", "surname", "wrongmail.it", userHandler.passwordEncryption("password"));
        UserDTO wrongDTO = new UserDTO(wrong);
        Throwable testException = assertThrows(ResponseStatusException.class, () -> userController.registerUser(wrongDTO), FootballRegisterException.INVALID_REGISTRATION_FIELDS.toString());
        assertThat(testException.getMessage(), containsString(FootballRegisterException.INVALID_REGISTRATION_FIELDS.toString()));


        //Try register user with right email
        User okUser = wrong;
        okUser.setEmail("email@email.it");
        UserDTO okDTO = new UserDTO(okUser);
        okDTO.setPassword("password");
        assertEquals(userController.registerUser(okDTO), "Successfully created user");

        //Try to register a new user with a mail already used by another user
        User secondUser = new User("second-username", "second-name", "second-surname", "email@email.it", userHandler.passwordEncryption("second-password"));
        UserDTO secondUserDTO = new UserDTO(secondUser);
        secondUserDTO.setPassword("second-password");
        testException = assertThrows(ResponseStatusException.class, () -> userController.registerUser(secondUserDTO), FootballRegisterException.EMAIL_ALREADY_EXIST.toString());
        assertThat(testException.getMessage(), containsString(FootballRegisterException.EMAIL_ALREADY_EXIST.toString()));

    }

    @Test
    public void bindUserWithEvent_general() throws InvalidKeySpecException, NoSuchAlgorithmException {
        //Try to bind not me to the event
        User wrongBindUser = new User("wrong", "wrongName", "wrongSurname", "wrong@email.it", userHandler.passwordEncryption("wrong-password"));
        User correctUser = new User("correct", "correctName", "correctSurname", "correct@email.it", userHandler.passwordEncryption("correct-password"));
        userRepository.save(wrongBindUser);
        userRepository.save(correctUser);
        Event event = eventRepository.save(new Event(Category.CALCIO_A_5, Instant.now().plus(2, ChronoUnit.HOURS), correctUser));
        Token userToken = tokenHandler.createToken(correctUser);
        UserEventDTO userEventDTO = new UserEventDTO(wrongBindUser.getUsername(), event.getId());
        Throwable testException = assertThrows(ResponseStatusException.class, () -> userController.bindUserAndEvent(userEventDTO, userToken), FootballRegisterException.PERMISSION_DENIED.toString());
        assertThat(testException.getMessage(), containsString(FootballRegisterException.PERMISSION_DENIED.toString()));


        //Try to register to event that will be played in less than 3 hours
        UserEventDTO correctDTO = new UserEventDTO(correctUser.getUsername(), event.getId());
        Token newToken = tokenHandler.createToken(correctUser);
        testException = assertThrows(ResponseStatusException.class, () -> userController.bindUserAndEvent(correctDTO, newToken), FootballRegisterException.CANNOT_REGISTER_USER.toString());
        assertThat(testException.getMessage(), containsString(FootballRegisterException.CANNOT_REGISTER_USER.toString()));
    }

    @Test
    public void bindUserWithEvent_alreadyRegisteredToValidEvent() throws InvalidKeySpecException, NoSuchAlgorithmException {
        //Try to bind not me to the event
        User user = new User("user", "userName", "userSurname", "user@email.it", userHandler.passwordEncryption("user-password"));
        userRepository.save(user);
        Event event1 = eventRepository.save(new Event(Category.CALCIO_A_5, Instant.now().plus(2, ChronoUnit.DAYS), user));
        Token userToken = tokenHandler.createToken(user);

        //Register user in event
        UserEventDTO correctDTO = new UserEventDTO(user.getUsername(), event1.getId());
        assertNotNull(userController.bindUserAndEvent(correctDTO, userToken));

        //Try to register to another event, shouldn't works
        Token newToken = tokenHandler.createToken(user);
        Event event2 = eventRepository.save(new Event(Category.CALCIO_A_5, Instant.now().plus(7, ChronoUnit.DAYS), user));
        UserEventDTO userEvent2 = new UserEventDTO(user.getUsername(),event2.getId());
        Throwable bindUserException = assertThrows(ResponseStatusException.class, () -> userController.bindUserAndEvent(userEvent2, newToken));
        assertThat(bindUserException.getMessage(), containsString(FootballRegisterException.CANNOT_REGISTER_USER.toString()));


        //Try to register to another event, should works
        Token token3 = tokenHandler.createToken(user);
        Event event3 = eventRepository.save(new Event(Category.CALCIO_A_5, Instant.now().plus(7, ChronoUnit.HOURS), user));
        UserEventDTO userEvent3 = new UserEventDTO(user.getUsername(),event3.getId());
        assertNotNull(userController.bindUserAndEvent(userEvent3, token3));

        //Check how many
        Token token4 = tokenHandler.createToken(user);
        List<Long> registeredUserEvents = userEventRepository.findByUser(user).stream().map(userEvent -> userEvent.getEvent().getId()).collect(Collectors.toList());
        List<EventDTO> registeredEventsDTO = userController.findBoundEvents(user.getUsername(), token4);
        assertEquals(registeredUserEvents.size(), 2);
        assertThat(registeredUserEvents.size(), equalTo(registeredEventsDTO.size()));
        assertTrue(registeredUserEvents.contains(event1.getId()));
        assertTrue(registeredUserEvents.contains(event3.getId()));
    }

    @Test
    public void removeBinding() throws InvalidKeySpecException, NoSuchAlgorithmException {
        //Bind user with event
        User user = new User("user", "userName", "userSurname", "user@email.it", userHandler.passwordEncryption("user-password"));
        userRepository.save(user);
        Event event = eventRepository.save(new Event(Category.CALCIO_A_5, Instant.now().plus(2, ChronoUnit.DAYS), user));
        Token userToken = tokenHandler.createToken(user);
        UserEventDTO correctDTO = new UserEventDTO(user.getUsername(), event.getId());
        assertNotNull(userController.bindUserAndEvent(correctDTO, userToken));

        //Check if binding recorded
        List<UserEvent> registeredEvents = userEventRepository.findByUser(user);
        assertEquals(registeredEvents.size(), 1);
        assertThat(registeredEvents.get(0).getUser().getId(), equalTo(user.getId()));
        assertThat(registeredEvents.get(0).getEvent().getId(), equalTo(event.getId()));

        //Removing binding
        userToken = tokenHandler.createToken(user);
        userController.removeBinding(user.getUsername(), event.getId(), userToken);

        //Check if DB is now empty
        registeredEvents = userEventRepository.findByUser(user);
        assertEquals(registeredEvents.size(), 0);
    }

    @Test
    public void deleteUser() throws InvalidKeySpecException, NoSuchAlgorithmException {
        User user = new User("user.user", "name", "surname", "user@email.it", userHandler.passwordEncryption("password"));
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", userHandler.passwordEncryption("password"));
        admin.setRole(Role.ADMIN);
        userRepository.save(user);
        userRepository.save(admin);
        Token userToken = tokenHandler.createToken(user);
        Token adminToken = tokenHandler.createToken(admin);
        Token methodToken = new Token(); //Token used for methods call, will be decrypted and changed with the property one each time

        //Try to delete user using user
        methodToken.token = userToken.token;
        Throwable testException = assertThrows(ResponseStatusException.class, () -> userController.deleteUser(user.getUsername(), methodToken));
        assertThat(testException.getMessage(), containsString(FootballRegisterException.PERMISSION_DENIED.toString()));
        assertThat(userRepository.findAllByIsActiveIsTrue().size(), equalTo(2));

        //Try to delete admin using user
        methodToken.token = userToken.token;
        testException = assertThrows(ResponseStatusException.class, () -> userController.deleteUser(admin.getUsername(), methodToken));
        assertThat(testException.getMessage(), containsString(FootballRegisterException.PERMISSION_DENIED.toString()));
        assertThat(userRepository.findAllByIsActiveIsTrue().size(), equalTo(2));

        //Try to delete user using admin
        methodToken.token = adminToken.token;
        assertNotNull(userController.deleteUser(user.getUsername(), methodToken));
        assertThat(userRepository.findAllByIsActiveIsTrue().size(), equalTo(1));

        //Try to delete admin using admin
        methodToken.token = adminToken.token;
        assertNotNull(userController.deleteUser(admin.getUsername(), methodToken));
        assertThat(userRepository.findAllByIsActiveIsTrue().size(), equalTo(0));
    }

    @Test
    public void findAllTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        User user = new User("user.user", "name", "surname", "user@email.it", userHandler.passwordEncryption("password"));
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", userHandler.passwordEncryption("password"));
        admin.setRole(Role.ADMIN);
        userRepository.save(user);
        userRepository.save(admin);
        Token userToken = tokenHandler.createToken(user);
        Token adminToken = tokenHandler.createToken(admin);

        //Try findall with user, he hasn't permissions to do that
        Throwable testException = assertThrows(ResponseStatusException.class, () -> userController.findAll(userToken));
        assertThat(testException.getMessage(), containsString(FootballRegisterException.PERMISSION_DENIED.toString()));

        //Try findAll with user with ADMIN permissions
        List<String> users = userController.findAll(adminToken).stream().map(UserDTO::getUsername).collect(Collectors.toList());
        assertThat(users.size(), equalTo(2));
        assertTrue(users.contains(user.getUsername()));
        assertTrue(users.contains(admin.getUsername()));
    }

    @Test
    public void testFindUser() throws InvalidKeySpecException, NoSuchAlgorithmException {
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", userHandler.passwordEncryption("password"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        Token adminToken = tokenHandler.createToken(admin);

        UserDTO userFromController = userController.findUser(admin.getUsername(), adminToken);
        assertEquals(admin.getUsername(), userFromController.getUsername());
        assertEquals(admin.getEmail(), userFromController.getEmail());
        assertEquals(admin.getName(), userFromController.getName());
        assertEquals(admin.getSurname(), userFromController.getSurname());
        assertEquals(admin.getRole().toString(), userFromController.getRole());
        assertTrue(userFromController.getIsActive());
        assertNull(userFromController.getPassword());
    }

    @Test
    public void testChangeRole() throws InvalidKeySpecException, NoSuchAlgorithmException {
        User user = new User("user.user", "name", "surname", "user@email.it", userHandler.passwordEncryption("password"));
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", userHandler.passwordEncryption("password"));
        admin.setRole(Role.ADMIN);
        user = userRepository.save(user);
        userRepository.save(admin);
        Token adminToken = tokenHandler.createToken(admin);
        Token tokenToUse = new Token();

        assertThat(userRepository.findById(user.getId()).get().getRole(), equalTo(Role.USER));

        tokenToUse.token = adminToken.token;
        userController.changeRole(user.getUsername(),tokenToUse);
        assertThat(userRepository.findById(user.getId()).get().getRole(), equalTo(Role.ADMIN));

        tokenToUse.token = adminToken.token;
        userController.changeRole(user.getUsername(),tokenToUse);
        assertThat(userRepository.findById(user.getId()).get().getRole(), equalTo(Role.USER));
    }
}
