package com.kongtoon.utils;

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
}
