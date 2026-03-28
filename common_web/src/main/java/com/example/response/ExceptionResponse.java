package com.example.response;

import java.io.Serializable;

import lombok.Getter;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;

@Getter
public class ExceptionResponse implements Serializable {
	private final String message;
	private final int errorCode;

	// 기본값: SERVER_ERROR
	public ExceptionResponse() {
		this.message = ErrorCode.SERVER_ERROR.getMessage();
		this.errorCode = ErrorCode.SERVER_ERROR.getCode();
	}

	// ErrorCode enum으로 생성
	public ExceptionResponse(ErrorCode errorCode) {
		this.message = errorCode.getMessage();
		this.errorCode = errorCode.getCode();
	}

	// CustomException으로 생성
	public ExceptionResponse(CustomException ex) {
		this.message = ex.getMessage();
		this.errorCode = ex.getErrorCode();
	}

	// 직접 지정 (기존 호환)
	public ExceptionResponse(String message, int errorCode) {
		this.message = message;
		this.errorCode = errorCode;
	}
}
