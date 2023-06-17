package com.kongtoon.domain.user.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kongtoon.utils.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SpringBootTest
class UserControllerTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

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
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		Optional<User> findUser = userRepository.findByLoginId(signupRequest.loginId());

		assertThat(findUser).isNotEmpty();

		resultActions.andExpect(status().isCreated());
		resultActions.andExpect(header().string("Location", "/users/signup/" + findUser.get().getId()));

		// docs
		resultActions.andDo(
				document("회원가입 성공",
						ResourceSnippetParameters.builder()
								.tag("회원가입")
								.summary("회원가입 성공, 실패 APIs")
								.requestSchema(Schema.schema("SignupRequest"))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("loginId").type(JsonFieldType.STRING).description(" 로그인ID"),
								fieldWithPath("name").type(JsonFieldType.STRING).description(" 이름"),
								fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
								fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네인"),
								fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
						),
						responseHeaders(
								headerWithName("Location").description("저장된 회원 URL")
						)
				)
		);
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
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath("message").value(ErrorCode.DUPLICATE_EMAIL.getMessage()));
		resultActions.andExpect(jsonPath("code").value(ErrorCode.DUPLICATE_EMAIL.name()));

		long afterUserCount = userRepository.count();
		assertThat(beforeUserCount).isSameAs(afterUserCount);

		// docs
		resultActions.andDo(
				document("이메일 중복으로 회원가입 실패",
						ResourceSnippetParameters.builder()
								.tag("회원가입")
								.requestSchema(Schema.schema("SignupRequest"))
								.responseSchema(Schema.schema("공통예외객체"))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("loginId").type(JsonFieldType.STRING).description(" 로그인ID"),
								fieldWithPath("name").type(JsonFieldType.STRING).description(" 이름"),
								fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
								fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네인"),
								fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
						),
						responseFields(
								fieldWithPath("message").type(JsonFieldType.STRING).description("실패 메세지"),
								fieldWithPath("code").type(JsonFieldType.STRING).description("실패 코드"),
								fieldWithPath("inputErrors").type(JsonFieldType.NULL).description("요청 에러 정보")
						)
				)
		);
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
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath("message").value(ErrorCode.DUPLICATE_LOGIN_ID.getMessage()));
		resultActions.andExpect(jsonPath("code").value(ErrorCode.DUPLICATE_LOGIN_ID.name()));

		long afterUserCount = userRepository.count();
		assertThat(beforeUserCount).isSameAs(afterUserCount);

		// docs
		resultActions.andDo(
				document("로그인ID 중복으로 회원가입 실패",
						ResourceSnippetParameters.builder()
								.tag("회원가입")
								.requestSchema(Schema.schema("SignupRequest"))
								.responseSchema(Schema.schema("공통예외객체"))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("loginId").type(JsonFieldType.STRING).description(" 로그인ID"),
								fieldWithPath("name").type(JsonFieldType.STRING).description(" 이름"),
								fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
								fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네인"),
								fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
						),
						responseFields(
								fieldWithPath("message").type(JsonFieldType.STRING).description("실패 메세지"),
								fieldWithPath("code").type(JsonFieldType.STRING).description("실패 코드"),
								fieldWithPath("inputErrors").type(JsonFieldType.NULL).description("요청 에러 정보")
						)
				)
		);
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
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup")
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

		// docs
		resultActions.andDo(
				document("이메일 형식에 맞지 않아 회원가입 실패",
						ResourceSnippetParameters.builder()
								.tag("회원가입")
								.requestSchema(Schema.schema("SignupRequest"))
								.responseSchema(Schema.schema("공통예외객체 상세"))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("loginId").type(JsonFieldType.STRING).description(" 로그인ID"),
								fieldWithPath("name").type(JsonFieldType.STRING).description(" 이름"),
								fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
								fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네인"),
								fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
						),
						responseFields(
								fieldWithPath("message").type(JsonFieldType.STRING).description("실패 메세지"),
								fieldWithPath("code").type(JsonFieldType.STRING).description("실패 코드"),
								fieldWithPath("inputErrors").type(JsonFieldType.ARRAY).description("요청 에러 정보"),
								fieldWithPath("inputErrors[].message").type(JsonFieldType.STRING).description("요청 에러 메세지"),
								fieldWithPath("inputErrors[].field").type(JsonFieldType.STRING).description("요청 에러 필드")
						)
				)
		);
	}

	@Test
	@DisplayName("로그인에 성공한다.")
	void loginSuccess() throws Exception {
		// given
		LoginRequest loginRequest = createLoginRequest();
		User user = createUser("email@email.com", loginRequest.loginId(), passwordEncoder.encrypt(loginRequest.password()));

		userRepository.save(user);

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)));

		// then
		resultActions.andExpect(status().isNoContent());

		// docs
		resultActions.andDo(
				document("로그인 성공",
						ResourceSnippetParameters.builder()
								.tag("로그인")
								.summary("로그인 성공, 실패 APIs")
								.requestSchema(Schema.schema("LoginRequest"))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("loginId").type(JsonFieldType.STRING).description("로그인ID"),
								fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
						)
				)
		);
	}

	@Test
	@DisplayName("로그인시 존재하지 않는 로그인 ID로 실패한다.")
	void loginNotExistLoginIdFail() throws Exception {
		// given
		LoginRequest loginRequest = createLoginRequest();

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)));

		// then
		resultActions.andExpect(status().isNotFound());
		resultActions.andExpect(jsonPath("message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
		resultActions.andExpect(jsonPath("code").value(ErrorCode.USER_NOT_FOUND.name()));

		// docs
		resultActions.andDo(
				document("로그인 ID가 존재하지 않아 로그인 실패",
						ResourceSnippetParameters.builder()
								.tag("로그인")
								.summary("로그인 성공, 실패 APIs")
								.requestSchema(Schema.schema("LoginRequest"))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("loginId").type(JsonFieldType.STRING).description("로그인ID"),
								fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
						),
						responseFields(
								fieldWithPath("message").type(JsonFieldType.STRING).description("실패 메세지"),
								fieldWithPath("code").type(JsonFieldType.STRING).description("실패 코드"),
								fieldWithPath("inputErrors").type(JsonFieldType.NULL).description("요청 에러 정보")
						)
				)
		);
	}

	@Test
	@DisplayName("로그인시 비밀번호 불일치로 실패한다.")
	void loginPasswordMismatchFail() throws Exception {
		// given
		LoginRequest loginRequest = createLoginRequest();
		User user = createUser("email@email.com", loginRequest.loginId(), passwordEncoder.encrypt("mismatchPassword"));

		userRepository.save(user);

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath("message").value(ErrorCode.LOGIN_FAIL.getMessage()));
		resultActions.andExpect(jsonPath("code").value(ErrorCode.LOGIN_FAIL.name()));

		// docs
		resultActions.andDo(
				document("비밀번호 불일치로 로그인 실패",
						ResourceSnippetParameters.builder()
								.tag("로그인")
								.summary("로그인 성공, 실패 APIs")
								.requestSchema(Schema.schema("LoginRequest"))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("loginId").type(JsonFieldType.STRING).description("로그인ID"),
								fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
						),
						responseFields(
								fieldWithPath("message").type(JsonFieldType.STRING).description("실패 메세지"),
								fieldWithPath("code").type(JsonFieldType.STRING).description("실패 코드"),
								fieldWithPath("inputErrors").type(JsonFieldType.NULL).description("요청 에러 정보")
						)
				)
		);
	}
}