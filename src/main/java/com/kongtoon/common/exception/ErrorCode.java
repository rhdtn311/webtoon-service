package com.kongtoon.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
	INVALID_INPUT("잘못된 입력값입니다.", 400),
	USER_NOT_FOUND("존재하지 않는 유저입니다.", 404),
	LOGIN_FAIL("아이디/비밀번호를 확인해주세요.", 409);

	private final String message;
	private final int status;

	ErrorCode(String message, int status) {
		this.message = message;
		this.status = status;
	}
}
