package com.example.user.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;

	public String generateToken(Long userId, String loginId, String role) {
		SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim("loginId", loginId)
			.claim("role", role)
			.issuedAt(now)
			.expiration(expiry)
			.signWith(key)
			.compact();
	}
}
