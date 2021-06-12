package com.runinng_app.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.util.HashMap;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ServerApplication {

	static HashMap<String, String> activeUsers = new HashMap<>();

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
