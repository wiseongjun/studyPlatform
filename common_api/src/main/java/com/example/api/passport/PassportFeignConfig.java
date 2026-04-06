package com.example.api.passport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PassportFeignConfig {

	@Bean
	public PassportRequestInterceptor passportRequestInterceptor() {
		return new PassportRequestInterceptor();
	}
}
