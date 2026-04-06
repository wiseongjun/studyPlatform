package com.example.gateway.security;

import java.util.Base64;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import com.example.gateway.security.dto.UserPassportDto;

@Component
@RequiredArgsConstructor
public class PassportSerializer {

	private final ObjectMapper objectMapper;

	public String serialize(UserPassportDto dto) {
		try {
			String json = objectMapper.writeValueAsString(dto);
			return Base64.getEncoder().encodeToString(json.getBytes());
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Passport 직렬화 실패", e);
		}
	}
}
