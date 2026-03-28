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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.ConstraintViolationException;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.response.ExceptionResponse;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private static HttpStatus resolveHttpStatus(CustomException exception) {
		return ErrorCode.findByCode(exception.getErrorCode())
			.map(code -> toSpringHttpStatus(code.getHttpStatus()))
			.orElse(HttpStatus.BAD_REQUEST);
	}

	private static HttpStatus toSpringHttpStatus(int statusCode) {
		HttpStatus resolved = HttpStatus.resolve(statusCode);
		return resolved != null ? resolved : HttpStatus.BAD_REQUEST;
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
	 * Entity의 Validation Exception 처리를 위한 핸들러
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ExceptionResponse> exception(ConstraintViolationException exception) {
		LOGGER.warn("ConstraintViolation: {}", exception.getMessage());
		HttpStatus status = toSpringHttpStatus(ErrorCode.INVALID_INPUT.getHttpStatus());
		return new ResponseEntity<>(new ExceptionResponse(ErrorCode.INVALID_INPUT), status);
	}

	/**
	 * Custom Exception 처리를 위한 핸들러
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ExceptionResponse> exception(CustomException exception) {
		HttpStatus status = resolveHttpStatus(exception);
		if (status.is4xxClientError()) {
			LOGGER.warn("BusinessException [{}]: {}", status.value(), exception.getMessage());
		} else {
			LOGGER.error("BusinessException [{}]: {}", status.value(), exception.getMessage(), exception);
		}
		return new ResponseEntity<>(new ExceptionResponse(exception), status);
	}

	/**
	 * 자바 관련 모든 Exception 처리를 위한 핸들러
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponse> exception(Exception exception) {
		LOGGER.error(" === [All Exception Log] " + exception.getMessage(), exception);
		HttpStatus status = toSpringHttpStatus(ErrorCode.SERVER_ERROR.getHttpStatus());
		return new ResponseEntity<>(new ExceptionResponse(ErrorCode.SERVER_ERROR), status);
	}

	/**
	 * 스프링 관련 모든 Exception 처리를 위한 핸들러
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
		HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
		if (statusCode instanceof HttpStatus httpStatus && httpStatus.is4xxClientError()) {
			LOGGER.warn("Web request error [{}]: {}", statusCode.value(), exception.getMessage());
		} else {
			LOGGER.error("Web request error [{}]: {}", statusCode.value(), exception.getMessage(), exception);
		}
		return super.handleExceptionInternal(exception, body, headers, statusCode, request);
	}

	/**
	 * Validation Exception 처리를 위한 핸들러
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		LOGGER.warn("MethodArgumentNotValid: {}", exception.getMessage());

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

		String message = sortedErrors.isEmpty()
			? ErrorCode.INVALID_INPUT.getMessage()
			: sortedErrors.get(0);
		HttpStatus httpStatus = toSpringHttpStatus(ErrorCode.INVALID_INPUT.getHttpStatus());
		return new ResponseEntity<>(
			new ExceptionResponse(message, ErrorCode.INVALID_INPUT.getCode()),
			headers,
			httpStatus
		);
	}

	/**
	 * 필수 요청 파라미터 누락 (Spring 6+ MissingServletRequestParameterException 전용 경로)
	 */
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
		MissingServletRequestParameterException exception, HttpHeaders headers, HttpStatusCode status,
		WebRequest request) {
		LOGGER.warn("MissingServletRequestParameter: {}", exception.getMessage());
		HttpStatus httpStatus = toSpringHttpStatus(ErrorCode.INVALID_INPUT.getHttpStatus());
		return new ResponseEntity<>(
			new ExceptionResponse(exception.getMessage(), ErrorCode.INVALID_INPUT.getCode()),
			headers,
			httpStatus
		);
	}

	/**
	 * Validation Exception 처리를 위한 핸들러
	 */
	@Override
	protected ResponseEntity<Object> handleServletRequestBindingException(
		ServletRequestBindingException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		LOGGER.warn("ServletRequestBinding: {}", exception.getMessage());
		HttpStatus httpStatus = toSpringHttpStatus(ErrorCode.INVALID_INPUT.getHttpStatus());
		return new ResponseEntity<>(
			new ExceptionResponse(exception.getMessage(), ErrorCode.INVALID_INPUT.getCode()),
			headers,
			httpStatus
		);
	}

}
