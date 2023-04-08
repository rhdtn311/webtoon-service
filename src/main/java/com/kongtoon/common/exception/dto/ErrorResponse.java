package com.kongtoon.common.exception.dto;

import java.util.List;

import org.springframework.validation.FieldError;

import lombok.AllArgsConstructor;
import lombok.Getter;

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

	public static ErrorResponse basic(String code, String message) {
		return new ErrorResponse(code, message);
	}

	public static ErrorResponse input(String code, String message, List<FieldError> filedErrors) {
		List<InputError> inputErrors = getInputErrors(filedErrors);

		return new ErrorResponse(code, message, inputErrors);
	}

	private static List<InputError> getInputErrors(List<FieldError> filedErrors) {
		return filedErrors.stream()
				.map(fieldError -> new InputError(fieldError.getDefaultMessage(), fieldError.getField()))
				.toList();
	}

	@Getter
	@AllArgsConstructor
	static class InputError {
		private final String message;
		private final String field;
	}
}
