package com.kongtoon.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
	INVALID_INPUT("잘못된 입력값입니다.", 400),
	METHOD_NOT_ALLOWED("지원하지 않은 HTTP 메소드입니다.", 400),
	AUTHENTICATION_FAIL("인증 실패입니다.", 401),
	UNAUTHORIZED("권한이 없습니다.", 403),
	SESSION_EXPIRATION("세션이 만료되었습니다,", 409),
	INTERNAL_SERVER_ERROR("예상치 못한 에러입니다.", 500),

	USER_NOT_FOUND("존재하지 않는 유저입니다.", 404),
	LOGIN_FAIL("아이디/비밀번호를 확인해주세요.", 409),
	DUPLICATE_LOGIN_ID("중복된 아이디입니다.", 409),
	DUPLICATE_EMAIL("중복된 이메일입니다.", 409),
	DUPLICATE_APPLY_AUTHOR_AUTHORITY("이미 작가인 유저입니다.", 409),

	AUTHOR_NOT_FOUND("존재하지 않는 작가입니다.", 404),

	COMIC_NOT_FOUND("존재하지 않는 웹툰입니다.", 404),
	NOT_EXISTS_THUMBNAIL_TYPE("존재하지 않는 썸네일 타입입니다.", 404),

	EPISODE_NOT_FOUND("존재하지 않는 에피소드입니다.", 404),

	DUPLICATE_FOLLOW("이미 팔로우 하고 있는 웹툰입니다.", 409),

	DUPLICATE_LIKE("이미 좋아요를 했습니다.", 409),

	FILE_NOT_UPLOAD("파일 업로드에 실패했습니다.", 409),
	FILE_NOT_DELETE("파일 삭제에 실패했습니다.", 409),
	NOT_ALLOWED_EXTENSION("허용된 확장자가 아닙니다.", 409),

	INCORRECT_TIME("유효하지 않는 시간입니다.", 409);

	private final String message;
	private final int status;

	ErrorCode(String message, int status) {
		this.message = message;
		this.status = status;
	}
}
