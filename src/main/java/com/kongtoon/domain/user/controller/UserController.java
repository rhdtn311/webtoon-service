package com.kongtoon.domain.user.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/login")
	public ResponseEntity<Void> login(
			@RequestBody @Valid LoginRequest loginRequest,
			HttpServletRequest httpServletRequest
	) {
		UserAuthDTO userAuth = userService.login(loginRequest);

		HttpSession session = httpServletRequest.getSession();
		UserSessionUtil.setLoginUserAuth(session, userAuth);

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpSession session) {
		UserSessionUtil.deleteLoginUserAuth(session);

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/signup")
	public ResponseEntity<Void> signup(
			@RequestBody @Valid SignupRequest signupRequest,
			HttpServletRequest httpServletRequest
	) {
		Long savedUserId = userService.signup(signupRequest);

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + "/" + savedUserId))
				.build();
	}

	@PostMapping("/signup/check-duplicate-id/{loginId}")
	public ResponseEntity<Void> checkDuplicateId(
			@PathVariable @NotBlank @Length(min = 5, max = 20) String loginId
	) {
		userService.validateDuplicateLoginId(loginId);

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/signup/check-duplicate-email/{email}")
	public ResponseEntity<Void> checkDuplicateEmail(
			@PathVariable @NotBlank @Email String email
	) {
		userService.validateDuplicateEmail(email);

		return ResponseEntity.noContent().build();
	}
}