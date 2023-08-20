package com.kongtoon.domain.user.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.common.security.PasswordEncoder;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.model.*;
import com.kongtoon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kongtoon.utils.TestConst.*;
import static com.kongtoon.utils.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SpringBootTest
class UserControllerTest {

	private static final String SIGNUP_TAG = "회원가입";
	private static final String SIGNUP_SUMMARY = "회원가입 성공, 실패 APIs";
	private static final String SIGNUP_REQ_SCHEMA = "SignupRequest";
	private static final String SIGNUP_LOGIN_ID_REQ_FIELD = "loginId";
	private static final String SIGNUP_LOGIN_ID_ID_VALUE_REQ_FIELD = "loginId.idValue";
	private static final String SIGNUP_NAME_REQ_FIELD = "name";
	private static final String SIGNUP_EMAIL_REQ_FIELD = "email";
	private static final String SIGNUP_EMAIL_ADDRESS_REQ_FIELD = "email.address";
	private static final String SIGNUP_NICKNAME_REQ_FIELD = "nickname";
	private static final String SIGNUP_PASSWORD_REQ_FIELD = "password";
	private static final String SIGNUP_PASSWORD_VALUE_REQ_FIELD = "password.passwordValue";
	private static final String SIGNUP_LOGIN_ID_REQ_DESCRIPTION = "로그인ID 정보";
	private static final String SIGNUP_LOGIN_ID_ID_VALUE_REQ_DESCRIPTION = "로그인ID";
	private static final String SIGNUP_NAME_REQ_DESCRIPTION = "이름";
	private static final String SIGNUP_EMAIL_REQ_DESCRIPTION = "이메일 정보";
	private static final String SIGNUP_EMAIL_ADDRESS_REQ_DESCRIPTION = "이메일 주소";
	private static final String SIGNUP_NICKNAME_REQ_DESCRIPTION = "닉네임";
	private static final String SIGNUP_PASSWORD_REQ_DESCRIPTION = "비밀번호 정보";
	private static final String SIGNUP_PASSWORD_VALUE_REQ_DESCRIPTION = "비밀번호";

	private static final String CHECK_LOGIN_ID_DUP_TAG = "로그인ID 중복 체크";
	private static final String CHECK_LOGIN_ID_DUP_SUMMARY = "로그인ID 중복 체크 성공, 실패 APIs";
	private static final String CHECK_LOGIN_ID_DUP_REQ_SCHEMA = "loginId";
	private static final String CHECK_LOGIN_ID_LOGIN_ID_REQ_FIELD = "loginId";
	private static final String CHECK_LOGIN_ID_LOGIN_ID_FIELD_DESCRIPTION = "로그인ID";

	private static final String CHECK_EMAIL_DUP_TAG = "이메일 중복 체크";
	private static final String CHECK_EMAIL_DUP_SUMMARY = "이메일 중복 체크 성공, 실패 APIs";
	private static final String CHECK_EMAIL_DUP_REQ_SCHEMA = "email";
	private static final String CHECK_EMAIL_EMAIL_REQ_FIELD = "email";
	private static final String CHECK_EMAIL_EMAIL_REQ_DESCRIPTION = "이메일";

	private static final String LOGIN_TAG = "로그인";
	private static final String LOGIN_SUMMARY = "로그인 성공, 실패 APIs";
	private static final String LOGIN_REQ_SCHEMA = "LoginRequest";
	private static final String LOGIN_LOGIN_ID_REQ_FIELD = "loginId";
	private static final String LOGIN_LOGIN_ID_ID_VALUE_REQ_FIELD = "loginId.idValue";
	private static final String LOGIN_PASSWORD_REQ_FIELD = "password";
	private static final String LOGIN_LOGIN_ID_REQ_DESCRIPTION = "로그인ID 정보";
	private static final String LOGIN_LOGIN_ID_ID_VALUE_REQ_DESCRIPTION = "로그인ID";
	private static final String LOGIN_PASSWORD_REQ_DESCRIPTION = "비밀번호";

	private static final String LOGOUT_TAG = "로그아웃";
	private static final String LOGOUT_SUMMARY = "로그아웃 성공, 실패 APIs";

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
								.tag(SIGNUP_TAG)
								.summary(SIGNUP_SUMMARY)
								.requestSchema(Schema.schema(SIGNUP_REQ_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath(SIGNUP_LOGIN_ID_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_LOGIN_ID_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_LOGIN_ID_ID_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_LOGIN_ID_ID_VALUE_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_NAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NAME_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_EMAIL_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_EMAIL_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_EMAIL_ADDRESS_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_EMAIL_ADDRESS_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_NICKNAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NICKNAME_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_PASSWORD_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_PASSWORD_VALUE_REQ_DESCRIPTION)
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
		Email savedEmail = new Email("savedEmail@email.com");
		LoginId savedLoginId = new LoginId("savedLoginId");

		User user = createUser(savedEmail, savedLoginId);
		userRepository.save(user);

		LoginId newLoginId = new LoginId("newLoginId");
		SignupRequest signupRequest = createSignupRequest(newLoginId, savedEmail);

		long beforeUserCount = userRepository.count();

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.DUPLICATE_EMAIL.getMessage()));
		resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.DUPLICATE_EMAIL.name()));

		long afterUserCount = userRepository.count();
		assertThat(beforeUserCount).isSameAs(afterUserCount);

		// docs
		resultActions.andDo(
				document("이메일 중복으로 회원가입 실패",
						ResourceSnippetParameters.builder()
								.tag(SIGNUP_TAG)
								.requestSchema(Schema.schema(SIGNUP_REQ_SCHEMA))
								.responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath(SIGNUP_LOGIN_ID_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_LOGIN_ID_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_LOGIN_ID_ID_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_LOGIN_ID_ID_VALUE_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_NAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NAME_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_EMAIL_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_EMAIL_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_EMAIL_ADDRESS_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_EMAIL_ADDRESS_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_NICKNAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NICKNAME_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_PASSWORD_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_PASSWORD_VALUE_REQ_DESCRIPTION)
						),
						responseFields(
								fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
								fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
								fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
						)
				)
		);
	}

	@Test
	@DisplayName("회원가입 시 로그인 아이디 중복으로 실패한다.")
	void signUpDuplicatedLoginIdFail() throws Exception {
		// given
		Email savedEmail = new Email("savedEmail@email.com");
		LoginId savedLoginId = new LoginId("savedLoginId");

		User user = createUser(savedEmail, savedLoginId);
		userRepository.save(user);

		Email newEmail = new Email("newEmail@email.com");
		SignupRequest signupRequest = createSignupRequest(savedLoginId, newEmail);

		long beforeUserCount = userRepository.count();

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.DUPLICATE_LOGIN_ID.getMessage()));
		resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.DUPLICATE_LOGIN_ID.name()));

		long afterUserCount = userRepository.count();
		assertThat(beforeUserCount).isSameAs(afterUserCount);

		// docs
		resultActions.andDo(
				document("로그인ID 중복으로 회원가입 실패",
						ResourceSnippetParameters.builder()
								.tag(SIGNUP_TAG)
								.requestSchema(Schema.schema(SIGNUP_REQ_SCHEMA))
								.responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath(SIGNUP_LOGIN_ID_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_LOGIN_ID_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_LOGIN_ID_ID_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_LOGIN_ID_ID_VALUE_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_NAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NAME_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_EMAIL_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_EMAIL_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_EMAIL_ADDRESS_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_EMAIL_ADDRESS_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_NICKNAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NICKNAME_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_PASSWORD_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_PASSWORD_VALUE_REQ_DESCRIPTION)
						),
						responseFields(
								fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
								fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
								fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
						)
				)
		);
	}

	@ParameterizedTest
	@MethodSource("invalidEmails")
	@DisplayName("회원가입 시 형식에 맞지 않은 이메일인 경우 실패한다.")
	void signUpInvalidEmailFail(Email invalidEmail) throws Exception {

		// given
		LoginId validLoginId = new LoginId("loginId");
		SignupRequest signupRequest = createSignupRequest(validLoginId, invalidEmail);
		long beforeUserCount = userRepository.count();

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));

		// then
		resultActions.andExpect(status().isBadRequest());
		resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.INVALID_INPUT.getMessage()));
		resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.INVALID_INPUT.name()));
		resultActions.andExpect(jsonPath(INPUT_ERROR_INFOS_MESSAGE_FIELD).value("잘못된 형식의 이메일입니다."));
		resultActions.andExpect(jsonPath(INPUT_ERROR_INFOS_FIELD_FIELD).value(SIGNUP_EMAIL_ADDRESS_REQ_FIELD));

		long afterUserCount = userRepository.count();
		assertThat(beforeUserCount).isSameAs(afterUserCount);

		// docs
		resultActions.andDo(
				document("이메일 형식에 맞지 않아 회원가입 실패",
						ResourceSnippetParameters.builder()
								.tag(SIGNUP_TAG)
								.requestSchema(Schema.schema(SIGNUP_REQ_SCHEMA))
								.responseSchema(Schema.schema(COMMON_EX_OBJ_DETAIL_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath(SIGNUP_LOGIN_ID_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_LOGIN_ID_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_LOGIN_ID_ID_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_LOGIN_ID_ID_VALUE_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_NAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NAME_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_EMAIL_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_EMAIL_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_EMAIL_ADDRESS_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_EMAIL_ADDRESS_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_NICKNAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NICKNAME_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_PASSWORD_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_PASSWORD_VALUE_REQ_DESCRIPTION)
						),
						responseFields(
								fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
								fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
								fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.ARRAY).description(INPUT_ERROR_INFOS_DESCRIPTION),
								fieldWithPath(INPUT_ERROR_INFOS_MESSAGE_FIELD).type(JsonFieldType.STRING).description(INPUT_ERROR_INFOS_MESSAGE_DESCRIPTION),
								fieldWithPath(INPUT_ERROR_INFOS_FIELD_FIELD).type(JsonFieldType.STRING).description(INPUT_ERROR_INFOS_FIELD_DESCRIPTION)
						)
				)
		);
	}

	private static Stream<Email> invalidEmails() {
		return Stream.of(
				new Email("notEmail"),
				new Email("notEmail@email"),
				new Email("notEmail.com")
		);
	}

	@Test
	@DisplayName("로그인ID 중복 체크에 성공한다.")
	void checkDuplicateLoginIdSuccess() throws Exception {

		// given
		String loginId = "loginId";

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup/check-duplicate-id/{loginId}", loginId));

		// then
		resultActions.andExpect(status().isNoContent());

		// docs
		resultActions.andDo(
				document("로그인ID가 중복되지 않아 로그인ID 중복 체크 성공",
						ResourceSnippetParameters.builder()
								.tag(CHECK_LOGIN_ID_DUP_TAG)
								.summary(CHECK_LOGIN_ID_DUP_SUMMARY)
								.requestSchema(Schema.schema(CHECK_LOGIN_ID_DUP_REQ_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						pathParameters(
								parameterWithName(CHECK_LOGIN_ID_LOGIN_ID_REQ_FIELD).description(CHECK_LOGIN_ID_LOGIN_ID_FIELD_DESCRIPTION)
						)
				)
		);
	}

	@Test
	@DisplayName("로그인 중복 체크에 실패한다.")
	void checkDuplicateLoginIdFail() throws Exception {

		// given
		LoginId loginId = new LoginId("dupLoginId");
		Email email = new Email("email@email.com");
		User user = createUser(email, loginId);
		userRepository.save(user);

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup/check-duplicate-id/{loginId}", loginId.getIdValue()));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.DUPLICATE_LOGIN_ID.getMessage()));
		resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.DUPLICATE_LOGIN_ID.name()));

		// docs
		resultActions.andDo(
				document("로그인ID가 중복되어 로그인ID 중복 체크 실패",
						ResourceSnippetParameters.builder()
								.tag(CHECK_LOGIN_ID_DUP_TAG)
								.summary(CHECK_LOGIN_ID_DUP_SUMMARY)
								.requestSchema(Schema.schema(CHECK_LOGIN_ID_DUP_REQ_SCHEMA))
								.responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						pathParameters(
								parameterWithName(CHECK_LOGIN_ID_LOGIN_ID_REQ_FIELD).description(CHECK_LOGIN_ID_LOGIN_ID_FIELD_DESCRIPTION)
						),
						responseFields(
								fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
								fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
								fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
						)
				)
		);
	}

	@Test
	@DisplayName("이메일 중복 체크에 성공한다.")
	void checkDuplicateEmailSuccess() throws Exception {

		// given
		String email = "email@email.com";

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup/check-duplicate-email/{email}", email));

		// then
		resultActions.andExpect(status().isNoContent());

		// docs
		resultActions.andDo(
				document("이메일이 중복되지 않아 이메일 중복 체크 성공",
						ResourceSnippetParameters.builder()
								.tag(CHECK_EMAIL_DUP_TAG)
								.summary(CHECK_EMAIL_DUP_SUMMARY)
								.requestSchema(Schema.schema(CHECK_EMAIL_DUP_REQ_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						pathParameters(
								parameterWithName(CHECK_EMAIL_EMAIL_REQ_FIELD).description(CHECK_EMAIL_EMAIL_REQ_DESCRIPTION)
						)
				)
		);
	}

	@Test
	@DisplayName("이메일 중복 체크에 실패한다.")
	void checkDuplicateEmailFail() throws Exception {

		// given
		Email email = new Email("dupEmail@email.com");
		LoginId loginId = new LoginId("loginId");
		User user = createUser(email, loginId);
		userRepository.save(user);

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup/check-duplicate-email/{email}", email.getAddress()));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath("message").value(ErrorCode.DUPLICATE_EMAIL.getMessage()));
		resultActions.andExpect(jsonPath("code").value(ErrorCode.DUPLICATE_EMAIL.name()));

		// docs
		resultActions.andDo(
				document("이메일이 중복되어 이메일 중복 체크 실패",
						ResourceSnippetParameters.builder()
								.tag(CHECK_EMAIL_DUP_TAG)
								.summary(CHECK_EMAIL_DUP_SUMMARY)
								.requestSchema(Schema.schema(CHECK_EMAIL_DUP_REQ_SCHEMA))
								.responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						pathParameters(
								parameterWithName(CHECK_EMAIL_EMAIL_REQ_FIELD).description(CHECK_EMAIL_EMAIL_REQ_DESCRIPTION)
						),
						responseFields(
								fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
								fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
								fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
						)
				)
		);
	}

	@Test
	@DisplayName("로그인에 성공한다.")
	void loginSuccess() throws Exception {
		// given
		LoginRequest loginRequest = createLoginRequest();
		Email email = new Email("email@email.com");
		Password password = new Password(passwordEncoder.encrypt(loginRequest.password().getPasswordValue()));
		User user = createUser(email, loginRequest.loginId(), password);

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
								.tag(LOGIN_TAG)
								.summary(LOGIN_SUMMARY)
								.requestSchema(Schema.schema(LOGIN_REQ_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath(LOGIN_LOGIN_ID_REQ_FIELD).type(JsonFieldType.OBJECT).description(LOGIN_LOGIN_ID_REQ_DESCRIPTION),
								fieldWithPath(LOGIN_LOGIN_ID_ID_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(LOGIN_LOGIN_ID_ID_VALUE_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_PASSWORD_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_PASSWORD_VALUE_REQ_DESCRIPTION)
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
		resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.USER_NOT_FOUND.getMessage()));
		resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.USER_NOT_FOUND.name()));

		// docs
		resultActions.andDo(
				document("로그인 ID가 존재하지 않아 로그인 실패",
						ResourceSnippetParameters.builder()
								.tag(LOGIN_TAG)
								.summary(LOGIN_SUMMARY)
								.requestSchema(Schema.schema(LOGIN_REQ_SCHEMA))
								.responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA))
						,
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath(LOGIN_LOGIN_ID_REQ_FIELD).type(JsonFieldType.OBJECT).description(LOGIN_LOGIN_ID_REQ_DESCRIPTION),
								fieldWithPath(LOGIN_LOGIN_ID_ID_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(LOGIN_LOGIN_ID_ID_VALUE_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_PASSWORD_REQ_DESCRIPTION),
								fieldWithPath(SIGNUP_PASSWORD_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_PASSWORD_VALUE_REQ_DESCRIPTION)
						),
						responseFields(
								fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
								fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
								fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
						)
				)
		);
	}

	@Test
	@DisplayName("로그인시 비밀번호 불일치로 실패한다.")
	void loginPasswordMismatchFail() throws Exception {
		// given
		LoginRequest loginRequest = createLoginRequest();
		Email email = new Email("email@email.com");
		Password mismatchPassword = new Password(passwordEncoder.encrypt("mismatchPassword"));
		User user = createUser(email, loginRequest.loginId(), mismatchPassword);

		userRepository.save(user);

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)));

		// then
		resultActions.andExpect(status().isConflict());
		resultActions.andExpect(jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.LOGIN_FAIL.getMessage()));
		resultActions.andExpect(jsonPath(ERROR_CODE_FIELD).value(ErrorCode.LOGIN_FAIL.name()));

		// docs
		resultActions.andDo(
				document("비밀번호 불일치로 로그인 실패",
						ResourceSnippetParameters.builder()
								.tag(LOGOUT_TAG)
								.summary(LOGOUT_SUMMARY)
						,
						responseFields(
								fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
								fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
								fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
						)
				)
		);
	}

	@Test
	@DisplayName("로그아웃에 성공한다.")
	void logoutSuccess() throws Exception {
		// given
		LoginId loginId = new LoginId("loginId");
		UserAuthDTO user = new UserAuthDTO(1L, loginId, UserAuthority.USER);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, user);

		// when
		ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/users/logout")
				.session(session)
		);

		// then
		resultActions.andExpect(status().isNoContent());
		assertThatThrownBy(() -> session.getAttribute(UserSessionUtil.LOGIN_MEMBER_ID))
				.isInstanceOf(IllegalStateException.class);

		// docs
		resultActions.andDo(
				document("로그아웃 성공",
						ResourceSnippetParameters.builder()
								.tag(LOGOUT_TAG)
								.summary(LOGOUT_SUMMARY)
				)
		);
	}
}