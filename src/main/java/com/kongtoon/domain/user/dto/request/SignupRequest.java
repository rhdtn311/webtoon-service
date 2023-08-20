package com.kongtoon.domain.user.dto.request;

import com.kongtoon.common.constant.RegexConst;
import com.kongtoon.domain.user.model.Email;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public record SignupRequest(
		@Valid
		LoginId loginId,

		@NotBlank
		String name,

		@Valid
		Email email,

		@NotBlank
		String nickname,

		@NotBlank
		@Pattern(regexp = RegexConst.PASSWORD_VALID_REGEX, message = "비밀번호 형식이 일치하지 않습니다.")
		@Length(min = 8, max = 30)
		String password
) {

	public User toEntity(String encryptPassword) {
		return new User(loginId, name, email, nickname, encryptPassword, UserAuthority.USER, true);
	}
}