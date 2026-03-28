package com.example.config;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.ConstraintViolationException;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.exception.ServerException;
import com.example.exception.UserException;
import com.example.response.ExceptionResponse;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * Entity의 Validation Exception 처리를 위한 핸들러
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ExceptionResponse> exception(ConstraintViolationException exception) {
		LOGGER.error(" === [Validation Exception Log] " + exception.getMessage(), exception);
		return new ResponseEntity<>(
			new ExceptionResponse(ErrorCode.INVALID_INPUT),
			HttpStatus.BAD_REQUEST
		);
	}

	/**
	 * User Exception 처리를 위한 핸들러 (CustomException보다 위에 선언)
	 */
	@ExceptionHandler(UserException.class)
	public ResponseEntity<ExceptionResponse> exception(UserException exception) {
		LOGGER.error(" === [UserException Log] " + exception.getMessage(), exception);
		return new ResponseEntity<>(
			new ExceptionResponse(exception),
			HttpStatus.UNAUTHORIZED
		);
	}

	/**
	 * Server Exception 처리를 위한 핸들러 (CustomException보다 위에 선언)
	 */
	@ExceptionHandler(ServerException.class)
	public ResponseEntity<ExceptionResponse> exception(ServerException exception) {
		LOGGER.error(" === [ServerException Log] " + exception.getMessage(), exception);
		return new ResponseEntity<>(
			new ExceptionResponse(exception),
			HttpStatus.INTERNAL_SERVER_ERROR
		);
	}

	/**
	 * Custom Exception 처리를 위한 핸들러
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ExceptionResponse> exception(CustomException exception) {
		LOGGER.error(" === [CustomException Log] " + exception.getMessage(), exception);
		return new ResponseEntity<>(
			new ExceptionResponse(exception),
			HttpStatus.BAD_REQUEST
		);
	}

	/**
	 * AccessDenied Exception 처리를 위한 핸들러
	 */
	// @ExceptionHandler(AccessDeniedException.class)
	// public ResponseEntity<ExceptionResponse> exception(AccessDeniedException exception) {
	// 	LOGGER.error(" === [AccessDeniedException Log] " + exception.getMessage(), exception);
	// 	return new ResponseEntity<>(
	// 		new ExceptionResponse(ErrorCode.UNAUTHORIZED),
	// 		HttpStatus.FORBIDDEN
	// 	);
	// }

	/**
	 * 자바 관련 모든 Exception 처리를 위한 핸들러
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponse> exception(Exception exception) {
		LOGGER.error(" === [All Exception Log] " + exception.getMessage(), exception);
		return new ResponseEntity<>(
			new ExceptionResponse(ErrorCode.SERVER_ERROR),
			HttpStatus.INTERNAL_SERVER_ERROR
		);
	}

	/**
	 * 스프링 관련 모든 Exception 처리를 위한 핸들러
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
		HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
		LOGGER.error(" === [WebRequest Exception Log] || " + request + " || " + exception.getMessage(), exception);
		return new ResponseEntity<>(
			new ExceptionResponse(ErrorCode.SERVER_ERROR),
			headers,
			statusCode
		);
	}

	/**
	 * Validation Exception 처리를 위한 핸들러
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		LOGGER.error(" === [Validation Exception Log2] || " + request + " || " + exception.getMessage(), exception);

		List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
		List<String> sortedErrors = fieldErrors.stream()
			.sorted((e1, e2) -> {
				try {
					Class<?> targetClass = Objects.requireNonNull(exception.getBindingResult().getTarget()).getClass();
					JsonPropertyOrder order = targetClass.getAnnotation(JsonPropertyOrder.class);
					if (order != null) {
						List<String> propertiesOrder = List.of(order.value());
						return Integer.compare(
							propertiesOrder.indexOf(e1.getField()),
							propertiesOrder.indexOf(e2.getField())
						);
					}
					return 0;
				} catch (Exception e) {
					return 0;
				}
			})
			.map(FieldError::getDefaultMessage)
			.toList();

		return new ResponseEntity<>(
			new ExceptionResponse(sortedErrors.get(0), ErrorCode.INVALID_INPUT.getCode()),
			headers,
			status
		);
	}

	/**
	 * Validation Exception 처리를 위한 핸들러
	 */
	@Override
	protected ResponseEntity<Object> handleServletRequestBindingException(
		ServletRequestBindingException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		LOGGER.error(" === [Validation Exception Log3] || " + request + " || " + exception.getMessage(), exception);
		return new ResponseEntity<>(
			new ExceptionResponse(exception.getMessage(), ErrorCode.INVALID_INPUT.getCode()),
			headers,
			status
		);
	}
}
