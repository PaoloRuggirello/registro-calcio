package com.elis.registrocalcio;

import com.elis.registrocalcio.handler.UserHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@SpringBootTest
class RegistroCalcioApplicationTests {

	@Autowired
	UserHandler userHandler;

	@Test
	public void cryptPassword() throws InvalidKeySpecException, NoSuchAlgorithmException {
		String password = "admin";
		String passwordEncrypted = userHandler.passwordEncryption(password);
		System.out.println(passwordEncrypted);
	}

}
