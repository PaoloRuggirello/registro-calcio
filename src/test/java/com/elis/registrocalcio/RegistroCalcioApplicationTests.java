package com.elis.registrocalcio;

import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.other.EmailServiceImpl;
import com.elis.registrocalcio.repository.general.UserRepository;
import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;

@SpringBootTest
class RegistroCalcioApplicationTests {

	@Autowired
	UserHandler userHandler;

	@Autowired
	UserRepository userRepository;
	@Autowired
    SecurityTokenRepository securityTokenRepository;
	@Autowired
	EmailServiceImpl emailService;

	@Test
	public void cryptPassword() throws InvalidKeySpecException, NoSuchAlgorithmException {
		String password = "admin";
		String passwordEncrypted = userHandler.passwordEncryption(password);
		System.out.println(passwordEncrypted);
	}

	@Test
	public void testDoubleDatasource(){
		SecurityToken token = new SecurityToken("test.user", Role.ADMIN, Instant.now());
		User paolo = new User("test.user", "test", "user", "testuser@mail.it", "password");

		securityTokenRepository.save(token);
		userRepository.save(paolo);

		Assertions.assertNotNull(securityTokenRepository.findAll());
		Assertions.assertNotNull(userRepository.findAll());
	}

	@Test
	public void sendEmail(){
		emailService.sendEmail();
	}

}
