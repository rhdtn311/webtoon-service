package com.kongtoon.domain.user.dto.request;

import com.kongtoon.domain.user.model.LoginId;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public record LoginRequest(
		@Valid
		LoginId loginId,

		@NotBlank
		String password) {
}