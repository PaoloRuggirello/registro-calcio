package com.elis.registrocalcio;

import com.elis.registrocalcio.controller.EventController;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.dto.UserDTO;
import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.TokenHandler;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
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
	TokenHandler tokenHandler;

	@Test
	public void cryptPassword() throws InvalidKeySpecException, NoSuchAlgorithmException {
		String password = "user";
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
//		System.out.println(event.getDate());
//		String realDateAsStrin = sDate.substring(0,24);
//		System.out.println(realDateAsStrin);

		SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		Date date = formatter.parse(event.date);
		Instant date2 = date.toInstant();
		System.out.println(date2);
		System.out.println(date);
	}

	@Test
	public void chekSameWeek() throws ParseException {
		String date1S = "Wed Feb 12 23:01:48 CET 2021";
		String date2S = "Wed Feb 09 23:01:48 CET 2021";
		String date3S = "Wed Feb 01 23:01:48 CET 2021";
		String date4S = "Wed Mar 08 23:01:48 CET 2020";
		SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		Instant date1 = formatter.parse(date1S).toInstant();
		Instant date2 = formatter.parse(date2S).toInstant();
		Instant date3 = formatter.parse(date3S).toInstant();
		Instant date4 = formatter.parse(date4S).toInstant();
		LocalDate localDate1 = LocalDate.ofInstant(date1, ZoneId.of("UTC"));
		LocalDate localDate2 = LocalDate.ofInstant(date2, ZoneId.of("UTC"));
		LocalDate localDate3 = LocalDate.ofInstant(date3, ZoneId.of("UTC"));
		LocalDate localDate4 = LocalDate.ofInstant(date4, ZoneId.of("UTC"));
		System.out.println(localDate2.getDayOfWeek().getValue());

		System.out.println("1,2: " + areInTheSameWeek(localDate1, localDate2));
		System.out.println("1,3: " + areInTheSameWeek(localDate1, localDate3));
		System.out.println("1,4: " + areInTheSameWeek(localDate1, localDate4));
		System.out.println("2,3: " + areInTheSameWeek(localDate2, localDate3));
		System.out.println("2,4: " + areInTheSameWeek(localDate2, localDate4));
		System.out.println("3,4: " + areInTheSameWeek(localDate3, localDate4));

	}

	private boolean areInTheSameWeek(LocalDate date1, LocalDate date2){ //Date2 is Always after date1
		if(date2.getDayOfWeek().getValue() < date1.getDayOfWeek().getValue()){
			LocalDate temp = date2;
			date2 = date1;
			date1 = temp;
		}
		int result = date2.getDayOfMonth() - date1.getDayOfMonth();
		if(date1.getYear() == date2.getYear() && date1.getMonth().getValue() == date2.getMonth().getValue() == result > 0 &&  result < 7)
			return true;
		else
			return false;
	}



//	@Test
//	public void sendEmail(){
//		emailService.sendEmail();
//	}
//
//	@Test
//	public void createEvent() throws SQLIntegrityConstraintViolationException {
//		User admin = userRepository.findByUsernameAndIsActiveIsTrue("admin.admin").get();
//		userRepository.save(admin);
//		Token adminToken = tokenHandler.createToken(admin);
//
//		EventDTO eventDTO = new EventDTO();
//		eventDTO.category = Category.CALCIO_A_7.toString();
//		eventDTO.creator = new UserDTO(admin);
//		eventDTO.date = Date.from(Instant.now().plus(3, ChronoUnit.DAYS));
//		Token token = new Token();
//		token.token = adminToken.token;
//		eventController.createEvent(eventDTO, token);
//	}

}
