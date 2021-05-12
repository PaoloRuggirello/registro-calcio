package com.elis.registrocalcio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class RegistroCalcioApplication {

	static {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		System.setProperty("currenttime", dateFormat.format(new Date()));
	}

	public static void main(String[] args) {
		SpringApplication.run(RegistroCalcioApplication.class, args);
	}

}
