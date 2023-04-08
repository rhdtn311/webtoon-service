package com.kongtoon.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kongtoon.common.exception.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(value = BusinessException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {

		ErrorCode errorCode = exception.getErrorCode();

		return ResponseEntity
				.status(HttpStatus.valueOf(errorCode.getStatus()))
				.body(ErrorResponse.basic(errorCode.name(), errorCode.getMessage()));
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.input(ErrorCode.INVALID_INPUT.name(), ErrorCode.INVALID_INPUT.getMessage(),
						exception.getFieldErrors()));
	}

	@ExceptionHandler(value = Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception exception) {

		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.basic("code", exception.getMessage()));
	}
}
