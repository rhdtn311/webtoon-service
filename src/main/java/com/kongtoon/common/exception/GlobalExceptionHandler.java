package com.kongtoon.common.exception;

import com.kongtoon.common.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;

import static com.kongtoon.common.exception.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.kongtoon.common.exception.ErrorCode.METHOD_NOT_ALLOWED;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(value = BusinessException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();

		return ResponseEntity
				.status(HttpStatus.valueOf(errorCode.getStatus()))
				.body(ErrorResponse.basic(errorCode));
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.input(exception.getFieldErrors()));
	}

	@ExceptionHandler(value = BindException.class)
	protected ResponseEntity<ErrorResponse> handleValidationException(BindException exception) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.input(exception.getFieldErrors()));
	}

	@ExceptionHandler(value = ConstraintViolationException.class)
	protected ResponseEntity<ErrorResponse> handleValidationException(ConstraintViolationException exception) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.input(exception.getConstraintViolations()));
	}

	@ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentTypeMismatchException exception) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.input(exception.getErrorCode(), exception.getParameter().getParameterName()));
	}

	@ExceptionHandler(value = MissingServletRequestParameterException.class)
	protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.input(exception.getMessage(), exception.getParameterName()));
	}

	@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.basic(METHOD_NOT_ALLOWED));
	}

	@ExceptionHandler(value = Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception exception) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.basic(INTERNAL_SERVER_ERROR));
	}
}
