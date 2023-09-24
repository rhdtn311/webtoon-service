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
import com.kongtoon.domain.user.model.Email;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.Password;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import com.kongtoon.support.dummy.UserDummy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kongtoon.utils.TestConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
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

	private static final String CHECK_LOGIN_ID_DUP_TAG = "로그인ID 중복 체크";
	private static final String CHECK_LOGIN_ID_DUP_SUMMARY = "로그인ID 중복 체크 성공, 실패 APIs";
	private static final String CHECK_LOGIN_ID_DUP_REQ_SCHEMA = "loginId";

	private static final String CHECK_EMAIL_DUP_TAG = "이메일 중복 체크";
	private static final String CHECK_EMAIL_DUP_SUMMARY = "이메일 중복 체크 성공, 실패 APIs";
	private static final String CHECK_EMAIL_DUP_REQ_SCHEMA = "email";

	private static final String LOGIN_TAG = "로그인";
	private static final String LOGIN_SUMMARY = "로그인 성공, 실패 APIs";
	private static final String LOGIN_REQ_SCHEMA = "LoginRequest";

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

	@Nested
	@Transactional
	@DisplayName("회원가입 성공")
	class SignupSuccess {

		@Test
		@DisplayName("회원가입에 성공한다.")
		void signupSuccess() throws Exception {
			// given
			SignupRequest signupRequest = UserDummy.createSignupRequest();

			// when
			ResultActions resultActions = requestSignup(signupRequest);

			// then
			Optional<User> findUser = userRepository.findByLoginId(signupRequest.loginId());

			assertThat(findUser).isNotEmpty();

			resultActions.andExpectAll(
					status().isCreated(),
					header().string("Location", "/users/signup/" + findUser.get().getId())
			);

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
							createSignupRequestDocs(),
							responseHeaders(
									headerWithName("Location").description("저장된 회원 URL")
							)
					)
			);
		}
	}

	@Nested
	@Transactional
	@DisplayName("회원가입 실패")
	class SignUpFail {

		@Test
		@DisplayName("회원가입 시 이메일 중복으로 실패한다.")
		void signUpDuplicatedEmailFail() throws Exception {
			// given
			Email savedEmail = new Email("savedEmail@email.com");
			LoginId savedLoginId = new LoginId("savedLoginId");

			User user = UserDummy.createUser(savedEmail, savedLoginId);
			userRepository.save(user);

			LoginId newLoginId = new LoginId("newLoginId");
			SignupRequest signupRequest = UserDummy.createSignupRequest(newLoginId, savedEmail);

			long beforeUserCount = userRepository.count();

			// when
			ResultActions resultActions = requestSignup(signupRequest);

			// then
			resultActions.andExpectAll(
					status().isConflict(),
					jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.DUPLICATE_EMAIL.getMessage()),
					jsonPath(ERROR_CODE_FIELD).value(ErrorCode.DUPLICATE_EMAIL.name())
			);

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
							createSignupRequestDocs(),
							createErrorResponseDocs()
					)
			);
		}

		@Test
		@DisplayName("회원가입 시 로그인 아이디 중복으로 실패한다.")
		void signUpDuplicatedLoginIdFail() throws Exception {
			// given
			Email savedEmail = new Email("savedEmail@email.com");
			LoginId savedLoginId = new LoginId("savedLoginId");

			User user = UserDummy.createUser(savedEmail, savedLoginId);
			userRepository.save(user);

			Email newEmail = new Email("newEmail@email.com");
			SignupRequest signupRequest = UserDummy.createSignupRequest(savedLoginId, newEmail);

			long beforeUserCount = userRepository.count();

			// when
			ResultActions resultActions = requestSignup(signupRequest);

			// then
			resultActions.andExpectAll(
					status().isConflict(),
					jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.DUPLICATE_LOGIN_ID.getMessage()),
					jsonPath(ERROR_CODE_FIELD).value(ErrorCode.DUPLICATE_LOGIN_ID.name())
			);

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
							createSignupRequestDocs(),
							createErrorResponseDocs()
					)
			);
		}

		@ParameterizedTest
		@MethodSource("invalidEmails")
		@DisplayName("회원가입 시 형식에 맞지 않은 이메일인 경우 실패한다.")
		void signUpInvalidEmailFail(Email invalidEmail) throws Exception {

			// given
			LoginId validLoginId = new LoginId("loginId");
			SignupRequest signupRequest = UserDummy.createSignupRequest(validLoginId, invalidEmail);
			long beforeUserCount = userRepository.count();

			// when
			ResultActions resultActions = requestSignup(signupRequest);

			// then
			resultActions.andExpectAll(
					status().isBadRequest(),
					jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.INVALID_INPUT.getMessage()),
					jsonPath(ERROR_CODE_FIELD).value(ErrorCode.INVALID_INPUT.name()),
					jsonPath(INPUT_ERROR_INFOS_MESSAGE_FIELD).value("잘못된 형식의 이메일입니다."),
					jsonPath(INPUT_ERROR_INFOS_FIELD_FIELD).value("email.address")
			);

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
							createSignupRequestDocs(),
							createInputErrorResponseDocs()
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
	}

	private static RequestFieldsSnippet createSignupRequestDocs() {
		return requestFields(
				fieldWithPath("loginId").type(JsonFieldType.OBJECT).description("로그인ID 정보"),
				fieldWithPath("loginId.idValue").type(JsonFieldType.STRING).description("로그인ID"),
				fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
				fieldWithPath("email").type(JsonFieldType.OBJECT).description("이메일 정보"),
				fieldWithPath("email.address").type(JsonFieldType.STRING).description("이메일 주소"),
				fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
				fieldWithPath("password").type(JsonFieldType.OBJECT).description("비밀번호 정보"),
				fieldWithPath("password.passwordValue").type(JsonFieldType.STRING).description("비밀번호")
		);
	}

	private ResultActions requestSignup(SignupRequest signupRequest) throws Exception {
		return mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)));
	}

	@Test
	@DisplayName("로그인ID 중복 체크에 성공한다.")
	void checkDuplicateLoginIdSuccess() throws Exception {

		// given
		String loginId = "loginId";

		// when
		ResultActions resultActions = requestCheckDuplicateLoginId(loginId);

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
						createCheckDuplicateLoginIdDocs()
				)
		);
	}

	@Test
	@DisplayName("로그인 중복 체크에 실패한다.")
	void checkDuplicateLoginIdFail() throws Exception {

		// given
		LoginId loginId = new LoginId("dupLoginId");
		Email email = new Email("email@email.com");
		User user = UserDummy.createUser(email, loginId);
		userRepository.save(user);

		// when
		ResultActions resultActions = requestCheckDuplicateLoginId(loginId.getIdValue());

		// then
		resultActions.andExpectAll(
				status().isConflict(),
				jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.DUPLICATE_LOGIN_ID.getMessage()),
				jsonPath(ERROR_CODE_FIELD).value(ErrorCode.DUPLICATE_LOGIN_ID.name())
		);

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
						createCheckDuplicateLoginIdDocs(),
						createErrorResponseDocs()
				)
		);
	}

	private ResultActions requestCheckDuplicateLoginId(String loginId) throws Exception {
		return mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup/check-duplicate-id/{loginId}", loginId));
	}

	private static PathParametersSnippet createCheckDuplicateLoginIdDocs() {
		return pathParameters(
				parameterWithName("loginId").description("로그인ID")
		);
	}

	@Test
	@DisplayName("이메일 중복 체크에 성공한다.")
	void checkDuplicateEmailSuccess() throws Exception {

		// given
		String email = "email@email.com";

		// when
		ResultActions resultActions = requestCheckDuplicateEmail(email);

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
						createCheckDuplicateEmailDocs()
				)
		);
	}

	@Test
	@DisplayName("이메일 중복 체크에 실패한다.")
	void checkDuplicateEmailFail() throws Exception {

		// given
		Email email = new Email("dupEmail@email.com");
		LoginId loginId = new LoginId("loginId");
		User user = UserDummy.createUser(email, loginId);
		userRepository.save(user);

		// when
		ResultActions resultActions = requestCheckDuplicateEmail(email.getAddress());

		// then
		resultActions.andExpectAll(
				status().isConflict(),
				jsonPath("message").value(ErrorCode.DUPLICATE_EMAIL.getMessage()),
				jsonPath("code").value(ErrorCode.DUPLICATE_EMAIL.name())
		);

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
						createCheckDuplicateEmailDocs(),
						createErrorResponseDocs()
				)
		);
	}

	private ResultActions requestCheckDuplicateEmail(String email) throws Exception {
		return mockMvc.perform(RestDocumentationRequestBuilders.post("/users/signup/check-duplicate-email/{email}", email));
	}

	private PathParametersSnippet createCheckDuplicateEmailDocs() {
		return pathParameters(
				parameterWithName("email").description("이메일")
		);
	}

	@Nested
	@Transactional
	@DisplayName("로그인 성공")
	class LoginSuccess {

		@Test
		@DisplayName("로그인에 성공한다.")
		void loginSuccess() throws Exception {
			// given
			LoginRequest loginRequest = UserDummy.createLoginRequest();
			Password password = new Password(passwordEncoder.encrypt(loginRequest.password().getPasswordValue()));
			User user = UserDummy.createUser(loginRequest.loginId(), password);

			userRepository.save(user);

			// when
			ResultActions resultActions = requestLogin(loginRequest);

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
							createLoginRequestDocs()
					)
			);
		}
	}

	@Nested
	@Transactional
	@DisplayName("로그인 실패")
	class LoginFail {

		@Test
		@DisplayName("로그인시 존재하지 않는 로그인 ID로 실패한다.")
		void loginNotExistLoginIdFail() throws Exception {
			// given
			LoginRequest loginRequest = UserDummy.createLoginRequest();

			// when
			ResultActions resultActions = requestLogin(loginRequest);

			// then
			resultActions.andExpectAll(
					status().isNotFound(),
					jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.USER_NOT_FOUND.getMessage()),
					jsonPath(ERROR_CODE_FIELD).value(ErrorCode.USER_NOT_FOUND.name())
			);

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
							createLoginRequestDocs(),
							createErrorResponseDocs()
					)
			);
		}

		@Test
		@DisplayName("로그인시 비밀번호 불일치로 실패한다.")
		void loginPasswordMismatchFail() throws Exception {
			// given
			LoginRequest loginRequest = UserDummy.createLoginRequest();
			Password mismatchPassword = new Password(passwordEncoder.encrypt("mismatchPassword"));
			User user = UserDummy.createUser(loginRequest.loginId(), mismatchPassword);

			userRepository.save(user);

			// when
			ResultActions resultActions = requestLogin(loginRequest);

			// then
			resultActions.andExpectAll(
					status().isConflict(),
					jsonPath(ERROR_MESSAGE_FIELD).value(ErrorCode.LOGIN_FAIL.getMessage()),
					jsonPath(ERROR_CODE_FIELD).value(ErrorCode.LOGIN_FAIL.name())
			);

			// docs
			resultActions.andDo(
					document("비밀번호 불일치로 로그인 실패",
							ResourceSnippetParameters.builder()
									.tag(LOGOUT_TAG)
									.summary(LOGOUT_SUMMARY)
							,
							createErrorResponseDocs()
					)
			);
		}
	}

	private ResultActions requestLogin(LoginRequest loginRequest) throws Exception {
		return mockMvc.perform(RestDocumentationRequestBuilders.post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)));
	}

	private RequestFieldsSnippet createLoginRequestDocs() {
		return requestFields(
				fieldWithPath("loginId").type(JsonFieldType.OBJECT).description("로그인ID 정보"),
				fieldWithPath("loginId.idValue").type(JsonFieldType.STRING).description("로그인ID"),
				fieldWithPath("password").type(JsonFieldType.OBJECT).description("비밀번호 정보"),
				fieldWithPath("password.passwordValue").type(JsonFieldType.STRING).description("비밀번호")
		);
	}

	@Test
	@DisplayName("로그아웃에 성공한다.")
	void logoutSuccess() throws Exception {
		// given
		UserAuthDTO user = UserDummy.createUserAuth();
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(UserSessionUtil.LOGIN_MEMBER_ID, user);

		// when
		ResultActions resultActions = requestLogout(session);

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

	private ResultActions requestLogout(MockHttpSession session) throws Exception {
		return mockMvc.perform(RestDocumentationRequestBuilders.post("/users/logout")
				.session(session)
		);
	}
}