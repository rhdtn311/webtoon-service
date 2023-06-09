package com.kongtoon.domain.user.service;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.kongtoon.utils.TestUtil.createLoginRequest;
import static com.kongtoon.utils.TestUtil.createUser;
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

	@Test
	@DisplayName("로그인에 성공한다.")
	void loginSuccess() {
		// given
		LoginRequest loginRequest = createLoginRequest();
		User user = createUser("email@email.com", loginRequest.loginId(), loginRequest.password());
		UserAuthDTO correctResult = new UserAuthDTO(0L, user.getLoginId(), user.getAuthority());

		when(userRepository.findByLoginId(loginRequest.loginId()))
				.thenReturn(Optional.of(user));
		when(passwordEncoder.isMatch(loginRequest.password(), user.getPassword()))
				.thenReturn(true);

		// when
		UserAuthDTO result = userService.login(loginRequest);

		// then
		verify(userRepository).findByLoginId(loginRequest.loginId());
		verify(passwordEncoder).isMatch(loginRequest.password(), user.getPassword());

		assertThat(result).usingRecursiveComparison()
				.ignoringFields("userId")
				.isEqualTo(correctResult);
	}

	@Test
	@DisplayName("로그인시 존재하지 않는 로그인 ID로 실패한다.")
	void loginNotExistLoginIdFail() {
		// given
		LoginRequest loginRequest = createLoginRequest();

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
		LoginRequest loginRequest = createLoginRequest();
		User user = createUser("email@email.com", loginRequest.loginId(), "mismatchPassword");

		when(userRepository.findByLoginId(loginRequest.loginId()))
				.thenReturn(Optional.of(user));
		when(passwordEncoder.isMatch(loginRequest.password(), user.getPassword()))
				.thenReturn(false);

		// when, hen
		assertThatThrownBy(() -> userService.login(loginRequest))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAIL);

		verify(userRepository).findByLoginId(loginRequest.loginId());
		verify(passwordEncoder).isMatch(loginRequest.password(), user.getPassword());
	}
}