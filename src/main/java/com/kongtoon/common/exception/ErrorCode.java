package com.kongtoon.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
	INVALID_INPUT("잘못된 입력값입니다.", 400),
	ENTITY_NOT_FOUND("데이터가 존재하지 않습니다.", 404);

	private final String message;
	private final int status;

	ErrorCode(String message, int status) {
		this.message = message;
		this.status = status;
	}
}
