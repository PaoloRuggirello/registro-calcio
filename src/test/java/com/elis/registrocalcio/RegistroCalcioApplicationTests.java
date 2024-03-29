package com.elis.registrocalcio;

import com.elis.registrocalcio.controller.EventController;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.EventHandler;
import com.elis.registrocalcio.handler.TokenHandler;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.other.EmailServiceImpl;
import com.elis.registrocalcio.other.DateUtils;
import com.elis.registrocalcio.repository.general.UserRepository;
import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
	@Autowired
	EventController eventController;
	@Autowired
	EventHandler eventHandler;
	@Autowired
	TokenHandler tokenHandler;

	@Test
	public void cryptPassword() throws InvalidKeySpecException, NoSuchAlgorithmException {
		String password = "ciao";
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
	public void testDate() throws ParseException {
		EventDTO event = new EventDTO();
		event.date = "Wed Feb 24 23:01:48 CET 2021";

		SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		Date date = formatter.parse(event.date);
		Instant date2 = date.toInstant();
		System.out.println(date2);
		System.out.println(date);
	}

	@Test
	public void testDateConversion(){
//		System.out.println(Instant.now().toString());
//		Instant date = Instant.parse("2021-05-26T20:44:14.000Z");
//		System.out.println(date);

		System.out.println(DateUtils.getDateFromInstant(Instant.now()));
	}

	@Test
	public void chekSameWeek() throws ParseException {
		String date1S = "Wed Feb 12 23:01:48 GMT 2021";
		String date2S = "Wed Feb 09 23:01:48 GMT 2021";
		String date3S = "Wed Feb 01 23:01:48 GMT 2021";
		String date4S = "Wed Mar 08 23:01:48 GMT 2020";
		SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		Instant date1 = formatter.parse(date1S).toInstant();
		Instant date2 = formatter.parse(date2S).toInstant();
		Instant date3 = formatter.parse(date3S).toInstant();
		Instant date4 = formatter.parse(date4S).toInstant();

		Assertions.assertTrue(DateUtils.areInTheSameWeek(date1, date2));
		Assertions.assertFalse(DateUtils.areInTheSameWeek(date1, date3));
		Assertions.assertFalse(DateUtils.areInTheSameWeek(date1, date4));
		Assertions.assertFalse(DateUtils.areInTheSameWeek(date2, date3));
		Assertions.assertFalse(DateUtils.areInTheSameWeek(date2, date4));
		Assertions.assertFalse(DateUtils.areInTheSameWeek(date3, date4));
	}

	@Test
	public void testingExport() throws DocumentException, IOException {
		User paolo = new User("test.user", "test", "user", "testuser@mail.it", "password");
		paolo = userRepository.save(paolo);
		Event event = new Event(Category.CALCIO_A_7, Instant.now(), paolo);
		eventHandler.exportEvent(event, "hello.pdf");
	}

	@Test
	public void testingList(){
		List<String> test = new ArrayList<>();
		test.add("1");
		test.add("2");

		System.out.println(test.subList(0, 10));
	}

	@Test
	public void test(){
		List<String> test = Arrays.asList("ciao", "due");
		Assertions.assertFalse(test.contains(null));
		Assertions.assertFalse(test.contains(""));
		Assertions.assertTrue(test.contains("CIAO"));

	}
}
