package com.example.exception;

public class UserException extends CustomException {

	public UserException() {
		super(ErrorCode.UNAUTHORIZED);
	}

	public UserException(Throwable cause) {
		super(ErrorCode.UNAUTHORIZED, cause);
	}

	public UserException(ErrorCode errorCode) {
		super(errorCode);
	}

	public UserException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
