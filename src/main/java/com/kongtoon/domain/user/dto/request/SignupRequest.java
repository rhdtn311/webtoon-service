package com.kongtoon.domain.user.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import com.kongtoon.common.constant.RegexConst;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;

public record SignupRequest(
		@NotBlank
		@Length(min = 5, max = 20)
		String loginId,

		@NotBlank
		String name,

		@NotBlank
		@Email
		String email,

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