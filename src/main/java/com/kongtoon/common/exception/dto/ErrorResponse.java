package com.kongtoon.common.exception.dto;

import com.kongtoon.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.util.List;
import java.util.Set;

import static com.kongtoon.common.exception.ErrorCode.INVALID_INPUT;

@Getter
public class ErrorResponse {

	private final String code;
	private final String message;
	private final List<InputError> inputErrors;

	private ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
		this.inputErrors = null;
	}

	private ErrorResponse(String code, String message, List<InputError> inputErrors) {
		this.code = code;
		this.message = message;
		this.inputErrors = inputErrors;
	}

	public static ErrorResponse basic(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage());
	}

	public static ErrorResponse input(List<FieldError> filedErrors) {
		List<InputError> inputErrors = getInputErrors(filedErrors);

		return new ErrorResponse(INVALID_INPUT.name(), INVALID_INPUT.getMessage(), inputErrors);
	}

	public static ErrorResponse input(Set<ConstraintViolation<?>> constraintViolations) {
		List<InputError> inputErrors = getInputErrors(constraintViolations);

		return new ErrorResponse(INVALID_INPUT.name(), INVALID_INPUT.getMessage(), inputErrors);
	}

	public static ErrorResponse input(String message, String invalidParameter) {
		return new ErrorResponse(INVALID_INPUT.name(), INVALID_INPUT.getMessage(), getInputErrors(message, invalidParameter));
	}

	private static List<InputError> getInputErrors(List<FieldError> filedErrors) {
		return filedErrors.stream()
				.map(fieldError -> new InputError(fieldError.getDefaultMessage(), fieldError.getField()))
				.toList();
	}

	private static List<InputError> getInputErrors(Set<ConstraintViolation<?>> constraintViolations) {
		return constraintViolations.stream()
				.map(constraintViolation -> new InputError(constraintViolation.getMessage(), getFieldByPath(constraintViolation.getPropertyPath())))
				.toList();
	}

	private static List<InputError> getInputErrors(String message, String invalidParameter) {
		return List.of(new InputError(message, invalidParameter));
	}

	private static String getFieldByPath(Path path) {
		return path.toString()
				.substring(path.toString().lastIndexOf(".") + 1);
	}

	@Getter
	@AllArgsConstructor
	static class InputError {
		private final String message;
		private final String field;
	}
}
