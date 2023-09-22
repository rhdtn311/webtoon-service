package com.kongtoon.domain.user.service;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.Email;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.Password;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import com.kongtoon.support.dummy.UserDummy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
		LoginId loginId = new LoginId("loginId");
		String name = "name";
		Email email = new Email("email@email.com");
		String nickname = "nickname";
		Password password = new Password("password");

		SignupRequest signupRequest = new SignupRequest(
				loginId, name, email, nickname, password
		);

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
		LoginId loginId = new LoginId("loginId");
		String name = "name";
		Email email = new Email("email@email.com");
		String nickname = "nickname";
		Password password = new Password("password");

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
		LoginId loginId = new LoginId("loginId");
		String name = "name";
		Email email = new Email("email@email.com");
		String nickname = "nickname";
		Password password = new Password("password");

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

	@Test
	@DisplayName("로그인에 성공한다.")
	void loginSuccess() {
		// given
		LoginRequest loginRequest = UserDummy.createLoginRequest();
		User user = UserDummy.createUser(loginRequest.loginId(), loginRequest.password());
		UserAuthDTO correctResult = UserDummy.createUserAuth(user.getLoginId(), user.getAuthority());

		when(userRepository.findByLoginId(loginRequest.loginId()))
				.thenReturn(Optional.of(user));
		when(passwordEncoder.isMatch(loginRequest.password().getPasswordValue(), user.getPassword().getPasswordValue()))
				.thenReturn(true);

		// when
		UserAuthDTO result = userService.login(loginRequest);

		// then
		verify(userRepository).findByLoginId(loginRequest.loginId());
		verify(passwordEncoder).isMatch(loginRequest.password().getPasswordValue(), user.getPassword().getPasswordValue());

		assertThat(result).usingRecursiveComparison()
				.ignoringFields("userId")
				.isEqualTo(correctResult);
	}

	@Test
	@DisplayName("로그인시 존재하지 않는 로그인 ID로 실패한다.")
	void loginNotExistLoginIdFail() {
		// given
		LoginRequest loginRequest = UserDummy.createLoginRequest();

		when(userRepository.findByLoginId(loginRequest.loginId()))
				.thenReturn(Optional.empty());

		// when, then
		assertThatThrownBy(() -> userService.login(loginRequest))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

		verify(userRepository).findByLoginId(loginRequest.loginId());
	}

	@Test
	@DisplayName("로그인시 비밀번호 불일치로 실패한다.")
	void loginPasswordMismatchFail() {
		// given
		LoginRequest loginRequest = UserDummy.createLoginRequest();
		Password mismatchPassword = new Password("mismatchPassword");
		User user = UserDummy.createUser(loginRequest.loginId(), mismatchPassword);

		when(userRepository.findByLoginId(loginRequest.loginId()))
				.thenReturn(Optional.of(user));
		when(passwordEncoder.isMatch(loginRequest.password().getPasswordValue(), user.getPassword().getPasswordValue()))
				.thenReturn(false);

		// when, hen
		assertThatThrownBy(() -> userService.login(loginRequest))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAIL);

		verify(userRepository).findByLoginId(loginRequest.loginId());
		verify(passwordEncoder).isMatch(loginRequest.password().getPasswordValue(), user.getPassword().getPasswordValue());
	}
}