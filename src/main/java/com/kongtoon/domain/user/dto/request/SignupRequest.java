package com.kongtoon.domain.user.dto.request;

import com.kongtoon.domain.user.model.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public record SignupRequest(
		@Valid
		LoginId loginId,

		@NotBlank
		String name,

		@Valid
		Email email,

		@NotBlank
		String nickname,

		@Valid
		Password password
) {

	public User toEntity() {
		return new User(loginId, name, email, nickname, password, UserAuthority.USER, true);
	}
}