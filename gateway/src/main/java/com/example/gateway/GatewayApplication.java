package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {
	// Spring Security 처리 시 Gateway에서 인증 및 정보 보내기
	// 사용자 데이터 쿠키, GlobalExeptionHandler 처리필요
	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}
