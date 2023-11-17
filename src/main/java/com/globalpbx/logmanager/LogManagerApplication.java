package com.globalpbx.logmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogManagerApplication.class, args);
	}

}
