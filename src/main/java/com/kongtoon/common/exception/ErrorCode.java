package com.kongtoon.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
	INVALID_INPUT("잘못된 입력값입니다.", 400),
	UNAUTHORIZED("인증 실패입니다.", 401),

	USER_NOT_FOUND("존재하지 않는 유저입니다.", 404),
	LOGIN_FAIL("아이디/비밀번호를 확인해주세요.", 409),
	DUPLICATE_LOGIN_ID("중복된 아이디입니다.", 409),
	DUPLICATE_EMAIL("중복된 이메일입니다.", 409),
	DUPLICATE_APPLY_AUTHOR_AUTHORITY("이미 작가인 유저입니다.", 409),

	AUTHOR_NOT_FOUND("존재하지 않는 작가입니다.", 404),

	FILE_NOT_UPLOAD("파일 업로드에 실패했습니다.", 409),
	FILE_NOT_DELETE("파일 삭제에 실패했습니다.", 409),
	NOT_ALLOWED_EXTENSION("허용된 확장자가 아닙니다.", 409);

	private final String message;
	private final int status;

	ErrorCode(String message, int status) {
		this.message = message;
		this.status = status;
	}
}
