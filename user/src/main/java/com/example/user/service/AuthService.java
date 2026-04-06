package com.example.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.user.dto.req.LoginRequest;
import com.example.user.dto.res.LoginResponse;
import com.example.user.entity.User;
import com.example.user.repository.UserJpaRepository;
import com.example.user.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserJpaRepository userJpaRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final BCryptPasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public LoginResult login(LoginRequest request) {
		User user = userJpaRepository.findByLoginId(request.getLoginId())
			.orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new CustomException(ErrorCode.LOGIN_FAILED);
		}

		String token = jwtTokenProvider.generateToken(user.getId(), user.getLoginId(), user.getRole().name());
		LoginResponse response = new LoginResponse(user.getId(), user.getLoginId(), user.getRole().name());
		return new LoginResult(token, response);
	}

	public record LoginResult(String token, LoginResponse response) {
	}
}
