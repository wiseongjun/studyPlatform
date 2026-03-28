package com.example.exception;

public class ServerException extends CustomException {

	public ServerException() {
		super(ErrorCode.SERVER_ERROR);
	}

	public ServerException(Throwable cause) {
		super(ErrorCode.SERVER_ERROR, cause);
	}
}

