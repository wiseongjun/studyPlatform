package com.example.exception;

import java.util.Optional;

import lombok.Getter;

@Getter
public enum ErrorCode {
	// Common
	SERVER_ERROR(0, "서버 오류입니다.", 500),
	INVALID_REQUEST(1, "잘못된 요청입니다.", 400),
	UNAUTHORIZED(2, "인증이 필요합니다.", 401),
	ENTITY_NOT_FOUND(3, "존재하지 않는 데이터입니다.", 404),
	INVALID_INPUT(4, "올바르지 않은 입력입니다.", 400),

	// 외부 연동 API
	EXTERNAL_API_ERROR(101, "외부 API 연동 중 실패했습니다.", 502),

	// Problem
	PROBLEM_NOT_FOUND(200, "존재하지 않는 문제입니다.", 404),
	NO_PROBLEM_AVAILABLE(201, "풀 수 있는 문제가 없습니다.", 404),

	// User
	USER_NOT_FOUND(301, "존재하지 않는 사용자입니다.", 404),
	LOGIN_FAILED(302, "아이디 또는 비밀번호가 올바르지 않습니다.", 401),

	// Chapter
	CHAPTER_NOT_FOUND(401, "존재하지 않는 단원입니다.", 404);

	private final int code;
	private final String message;
	private final int httpStatus;

	ErrorCode(int code, String message, int httpStatus) {
		this.code = code;
		this.message = message;
		this.httpStatus = httpStatus;
	}

	public static Optional<ErrorCode> findByCode(int code) {
		for (ErrorCode value : values()) {
			if (value.getCode() == code) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}
}
