package com.example.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.example.user.security.JwtProperties;

@SpringBootApplication(scanBasePackages = "com.example")
@EnableFeignClients(basePackages = "com.example.api")
@EnableConfigurationProperties(JwtProperties.class)
public class UserDetailApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserDetailApplication.class, args);
	}

}
