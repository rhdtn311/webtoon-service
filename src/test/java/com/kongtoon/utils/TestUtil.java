package com.kongtoon.utils;

import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;

public class TestUtil {

	public static SignupRequest createSignupRequest() {
		return new SignupRequest(
				"loginId",
				"Name",
				"email@email.com",
				"nickname",
				"password123!"
		);
	}

	public static SignupRequest createSignupRequest(String loginId, String email) {
		return new SignupRequest(
				loginId,
				"Name",
				email,
				"nickname",
				"password123!"
		);
	}

	public static User createUser(String email, String loginId) {
		return new User(
				loginId,
				"Name",
				email,
				"nickname",
				"password123!",
				UserAuthority.USER,
				true
		);
	}

	public static User createUser(String email, String loginId, String password) {
		return new User(
				loginId,
				"Name",
				email,
				"nickname",
				password,
				UserAuthority.USER,
				true
		);
	}

	public static LoginRequest createLoginRequest() {
		return new LoginRequest(
				"loginId", "password"
		);
	}
}
