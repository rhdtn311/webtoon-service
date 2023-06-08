package com.kongtoon.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@InjectMocks
	UserService userService;

	@Test
	@DisplayName("회원가입에 성공한다.")
	void signupSuccess() {
		// given
		String loginId = "loginId";
		String name = "name";
		String email = "email@email.com";
		String nickname = "nickname";
		String password = "password";
		String encryptedPassword = "encryptedPassword";

		SignupRequest signupRequest = new SignupRequest(
				loginId, name, email, nickname, password
		);

		when(passwordEncoder.encrypt(password))
				.thenReturn(encryptedPassword);
		when(userRepository.existsByLoginId(loginId))
				.thenReturn(false);
		when(userRepository.existsByEmail(email))
				.thenReturn(false);

		// when
		userService.signup(signupRequest);

		// then
		verify(userRepository).save(any(User.class));
		verify(userRepository).existsByLoginId(loginId);
		verify(userRepository).existsByEmail(email);
	}

	@Test
	@DisplayName("회원가입 시 이메일 중복으로 실패한다.")
	void signUpDuplicatedEmailFail() {
		// given
		String loginId = "loginId";
		String name = "name";
		String email = "email@email.com";
		String nickname = "nickname";
		String password = "password";

		SignupRequest signupRequest = new SignupRequest(
				loginId, name, email, nickname, password
		);

		when(userRepository.existsByEmail(email))
				.thenReturn(true);

		// when, then
		assertThatThrownBy(() -> userService.signup(signupRequest))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

		verify(userRepository).existsByEmail(email);
	}

	@Test
	@DisplayName("회원가입 시 로그인 아이디 중복으로 실패한다.")
	void signUpDuplicatedLoginIdFail() {
		// given
		String loginId = "loginId";
		String name = "name";
		String email = "email@email.com";
		String nickname = "nickname";
		String password = "password";

		SignupRequest signupRequest = new SignupRequest(
				loginId, name, email, nickname, password
		);

		when(userRepository.existsByEmail(email))
				.thenReturn(false);
		when(userRepository.existsByLoginId(loginId))
				.thenReturn(true);

		// when, then
		assertThatThrownBy(() -> userService.signup(signupRequest))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_LOGIN_ID);

		verify(userRepository).existsByEmail(email);
		verify(userRepository).existsByLoginId(loginId);
	}
}