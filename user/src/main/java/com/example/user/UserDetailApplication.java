package com.example.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.example")
@EnableFeignClients(basePackages = "com.example.api")
public class UserDetailApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserDetailApplication.class, args);
	}

}
