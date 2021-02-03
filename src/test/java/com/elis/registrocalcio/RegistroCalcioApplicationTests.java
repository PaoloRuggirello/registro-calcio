package com.elis.registrocalcio;

import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.security.Token;
import com.elis.registrocalcio.repository.general.UserRepository;
import com.elis.registrocalcio.repository.security.TokenRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

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
	TokenRepository tokenRepository;

	@Test
	public void cryptPassword() throws InvalidKeySpecException, NoSuchAlgorithmException {
		String password = "admin";
		String passwordEncrypted = userHandler.passwordEncryption(password);
		System.out.println(passwordEncrypted);
	}

	@Test
	public void testDoubleDatasource(){
		Token token = new Token("paolo.ruggirello", Role.ADMIN.toString(), Instant.now());
		User paolo = new User("paolo.ruggirelloa", "paolo", "ruggirello", "ruggirello999@live.it", "password");

		tokenRepository.save(token);
		userRepository.save(paolo);

		Assertions.assertNotNull(tokenRepository.findAll());
		Assertions.assertNotNull(userRepository.findAll());
	}

}
