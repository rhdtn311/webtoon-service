package com.kongtoon.domain.user.dto.request;

import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.Password;

import javax.validation.Valid;

public record LoginRequest(
		@Valid
		LoginId loginId,

		@Valid
		Password password
) {
}