package com.app.shecare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ShecareApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShecareApplication.class, args);
	}

}
