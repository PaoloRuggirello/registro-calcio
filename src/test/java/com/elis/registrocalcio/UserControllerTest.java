package com.elis.registrocalcio;

import com.elis.registrocalcio.controller.UserController;
import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.dto.UserDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.TokenHandler;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.repository.general.UserRepository;
import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static com.elis.registrocalcio.model.security.SecurityToken.getTokenExpirationDate;
import static com.elis.registrocalcio.model.security.SecurityToken.getTokenId;

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

    @Test
    public void testAuthentication() throws InvalidKeySpecException, NoSuchAlgorithmException {
        //Check Auth ok
        User userToAuthenticate = new User("user.user", "user", "user", "user@email.it", userHandler.passwordEncryption("password"));
        userToAuthenticate = userRepository.save(userToAuthenticate);
        UserDTO toAuthenticate = new UserDTO(userToAuthenticate.getUsername(), "password");
        assertNotNull(userController.authenticate(toAuthenticate));

        //Check username empty
        UserDTO wrong = new UserDTO("", userHandler.passwordEncryption("password"));
        assertThrows(ResponseStatusException.class, () -> userController.authenticate(wrong), FootballRegisterException.INVALID_LOGIN_FIELDS.toString());

        //Check password null
        wrong.setUsername("username");
        wrong.setPassword(null);
        assertThrows(ResponseStatusException.class, () -> userController.authenticate(wrong), FootballRegisterException.INVALID_LOGIN_FIELDS.toString());

        //Check authentication fail, invalid username or password
        wrong.setPassword(userHandler.passwordEncryption("password"));
        assertNull(userController.authenticate(wrong));
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
        assertThrows(ResponseStatusException.class, () -> userController.findAll(userToken), FootballRegisterException.INVALID_TOKEN.toString());

        //CheckPermissionLevel
        testUser.setRole(Role.USER);
        userRepository.save(testUser);
        Token newUserToken = tokenHandler.createToken(testUser);
        assertThrows(ResponseStatusException.class, () -> userController.findAll(newUserToken), FootballRegisterException.PERMISSION_DENIED.toString());
    }
}
