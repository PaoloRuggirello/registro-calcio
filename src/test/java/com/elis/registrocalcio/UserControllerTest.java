package com.elis.registrocalcio;

import com.elis.registrocalcio.controller.UserController;
import com.elis.registrocalcio.dto.UserDTO;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.repository.general.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@SpringBootTest
public class UserControllerTest {
    @Autowired
    UserController userController;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserHandler userHandler;

    @Test
    public void testAuthentication() throws InvalidKeySpecException, NoSuchAlgorithmException {
        UserDTO empty = new UserDTO();
        Assertions.assertThrows(ResponseStatusException.class, () -> userController.authenticate(empty));
        User userToAuthenticate = new User("user.user", "user", "user", "user@email.it", userHandler.passwordEncryption("password"));
        userToAuthenticate = userRepository.save(userToAuthenticate);
        UserDTO toAuthenticate = new UserDTO(userToAuthenticate.getUsername(), "password");
        Assertions.assertNotNull(userController.authenticate(toAuthenticate));
    }
}
