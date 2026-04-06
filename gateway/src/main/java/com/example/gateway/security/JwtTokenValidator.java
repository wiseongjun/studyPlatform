package com.example.gateway.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import com.example.gateway.security.dto.UserPassportDto;

@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

	private final JwtProperties jwtProperties;

	public boolean validateToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
			Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public UserPassportDto parsePayload(String token) {
		SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
		Claims claims = Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();

		Long userId = Long.parseLong(claims.getSubject());
		String loginId = claims.get("loginId", String.class);
		String role = claims.get("role", String.class);

		return new UserPassportDto(userId, loginId, role);
	}
}
