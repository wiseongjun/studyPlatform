package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openApi() {
		return new OpenAPI()
			.addServersItem(new Server().url("/"));
	}
}
