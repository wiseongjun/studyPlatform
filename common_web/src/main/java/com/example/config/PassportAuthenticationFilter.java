package com.example.config;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.api.passport.PassportSerializer;
import com.example.api.passport.UserContext;
import com.example.api.passport.dto.UserPassportDto;

@Component
public class PassportAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(PassportAuthenticationFilter.class);
	private static final String PASSPORT_HEADER = "X-User-Passport";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			String encoded = request.getHeader(PASSPORT_HEADER);
			if (encoded != null && !encoded.isBlank()) {
				UserPassportDto passport = PassportSerializer.deserialize(encoded);
				UserContext.setUserPassport(passport);
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
					passport.getLoginId(),
					null,
					List.of(new SimpleGrantedAuthority(passport.getRole()))
				);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			LOGGER.warn("Passport 헤더 파싱 실패: {}", e.getMessage());
			filterChain.doFilter(request, response);
		} finally {
			UserContext.clearUserPassport();
		}
	}
}
