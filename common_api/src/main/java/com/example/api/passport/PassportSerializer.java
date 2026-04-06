package com.example.api.passport;

import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.api.passport.dto.UserPassportDto;

public class PassportSerializer {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private PassportSerializer() {
	}

	public static String serialize(UserPassportDto dto) {
		try {
			String json = OBJECT_MAPPER.writeValueAsString(dto);
			return Base64.getEncoder().encodeToString(json.getBytes());
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Passport 직렬화 실패", e);
		}
	}

	public static UserPassportDto deserialize(String encoded) {
		try {
			byte[] decoded = Base64.getDecoder().decode(encoded);
			return OBJECT_MAPPER.readValue(decoded, UserPassportDto.class);
		} catch (Exception e) {
			throw new IllegalArgumentException("Passport 역직렬화 실패", e);
		}
	}
}
