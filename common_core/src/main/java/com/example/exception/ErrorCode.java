package com.example.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
	// Common
	SERVER_ERROR(0, "서버 오류입니다."),
	INVALID_REQUEST(1, "잘못된 요청입니다."),
	UNAUTHORIZED(2, "인증이 필요합니다."),
	ENTITY_NOT_FOUND(3, "존재하지 않는 데이터입니다."),
	INVALID_INPUT(4, "올바르지 않은 입력입니다."),

	// 외부 연동 API
	EXTERNAL_API_ERROR(101, "외부 API 연동 중 실패했습니다."),

	// Problem
	PROBLEM_NOT_FOUND(200, "존재하지 않는 문제입니다."),
	NO_PROBLEM_AVAILABLE(201, "풀 수 있는 문제가 없습니다."),

	// User
	USER_NOT_FOUND(301, "존재하지 않는 사용자입니다."),

	// Chapter
	CHAPTER_NOT_FOUND(401, "존재하지 않는 단원입니다.");

	private final int code;
	private final String message;

	ErrorCode(int code, String message) {
		this.code = code;
		this.message = message;
	}
}
