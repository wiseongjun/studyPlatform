package com.example.user.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.example.user.dto.req.LoginRequest;
import com.example.user.dto.res.LoginResponse;
import com.example.user.service.AuthService;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "로그인", description = "loginId와 password로 로그인합니다. access_token 쿠키가 설정됩니다.")
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthService.LoginResult result = authService.login(request);

		ResponseCookie cookie = ResponseCookie.from("access_token", result.token())
			.httpOnly(true)
			.sameSite("Lax")
			.maxAge(86400)
			.path("/")
			.build();

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(result.response());
	}
}
