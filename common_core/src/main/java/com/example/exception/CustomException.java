package com.example.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
	private final int errorCode;

	public CustomException() {
		super(ErrorCode.SERVER_ERROR.getMessage());
		this.errorCode = ErrorCode.SERVER_ERROR.getCode();
	}

	public CustomException(Throwable cause) {
		super(ErrorCode.SERVER_ERROR.getMessage(), cause);
		this.errorCode = ErrorCode.SERVER_ERROR.getCode();
	}

	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode.getCode();
	}

	public CustomException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode.getCode();
	}
}
