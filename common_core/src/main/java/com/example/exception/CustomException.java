package com.example.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
	private final String message;
	private final int errorCode;

	// 기본값: SERVER_ERROR
	public CustomException() {
		this.message = ErrorCode.SERVER_ERROR.getMessage();
		this.errorCode = ErrorCode.SERVER_ERROR.getCode();
	}

	public CustomException(Throwable cause) {
		super(cause);
		this.message = ErrorCode.SERVER_ERROR.getMessage();
		this.errorCode = ErrorCode.SERVER_ERROR.getCode();
	}

	// ErrorCode enum으로 생성
	public CustomException(ErrorCode errorCode) {
		this.message = errorCode.getMessage();
		this.errorCode = errorCode.getCode();
	}

	public CustomException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.message = errorCode.getMessage();
		this.errorCode = errorCode.getCode();
	}
}
