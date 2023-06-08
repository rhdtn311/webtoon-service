package com.kongtoon.domain.user.controller;

import static com.kongtoon.utils.TestUtil.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SpringBootTest
class UserControllerTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	@DisplayName("회원가입에 성공한다.")
	void signupSuccess() throws Exception {
		// given
		SignupRequest signupRequest = createSignupRequest();

		// when
		ResultActions resultActions = mockMvc.perform(post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		Optional<User> findUser = userRepository.findByLoginId(signupRequest.loginId());

		assertThat(findUser).isNotEmpty();

		resultActions.andExpect(status().isCreated());
		resultActions.andExpect(header().string("Location", "/users/signup/" + findUser.get().getId()));
	}

	@Test
	@DisplayName("회원가입 시 이메일 중복으로 실패한다.")
	void signUpDuplicatedEmailFail() throws Exception {
		// given
		String savedEmail = "savedEmail@email.com";
		String savedLoginId = "savedLoginId";

		User user = createUser(savedEmail, savedLoginId);
		userRepository.save(user);

		String newLoginId = "newLoginId";
		SignupRequest signupRequest = createSignupRequest(newLoginId, savedEmail);

		long beforeUserCount = userRepository.count();

		// when
		ResultActions resultActions = mockMvc.perform(post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath("message").value(ErrorCode.DUPLICATE_EMAIL.getMessage()));
		resultActions.andExpect(jsonPath("code").value(ErrorCode.DUPLICATE_EMAIL.name()));

		long afterUserCount = userRepository.count();
		assertThat(beforeUserCount).isSameAs(afterUserCount);
	}

	@Test
	@DisplayName("회원가입 시 로그인 아이디 중복으로 실패한다.")
	void signUpDuplicatedLoginIdFail() throws Exception {
		// given
		String savedEmail = "savedEmail@email.com";
		String savedLoginId = "savedLoginId";

		User user = createUser(savedEmail, savedLoginId);
		userRepository.save(user);

		String newEmail = "newEmail@email.com";
		SignupRequest signupRequest = createSignupRequest(savedLoginId, newEmail);

		long beforeUserCount = userRepository.count();

		// when
		ResultActions resultActions = mockMvc.perform(post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath("message").value(ErrorCode.DUPLICATE_LOGIN_ID.getMessage()));
		resultActions.andExpect(jsonPath("code").value(ErrorCode.DUPLICATE_LOGIN_ID.name()));

		long afterUserCount = userRepository.count();
		assertThat(beforeUserCount).isSameAs(afterUserCount);
	}

	@ParameterizedTest
	@ValueSource(strings = {"notEmail", "notEmail@email", "notEmail.com"})
	@DisplayName("회원가입 시 형식에 맞지 않은 이메일인 경우 실패한다.")
	void signUpInvalidEmailFail(String invalidEmail) throws Exception {

		// given
		String validLoginId = "loginId";
		SignupRequest signupRequest = createSignupRequest(validLoginId, invalidEmail);
		long beforeUserCount = userRepository.count();

		// when
		ResultActions resultActions = mockMvc.perform(post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		resultActions.andExpect(status().isBadRequest());
		resultActions.andExpect(jsonPath("message").value(ErrorCode.INVALID_INPUT.getMessage()));
		resultActions.andExpect(jsonPath("code").value(ErrorCode.INVALID_INPUT.name()));
		resultActions.andExpect(jsonPath("inputErrors[0].message").value("잘못된 형식의 이메일입니다."));
		resultActions.andExpect(jsonPath("inputErrors[0].field").value("email"));

		long afterUserCount = userRepository.count();
		assertThat(beforeUserCount).isSameAs(afterUserCount);
	}
}