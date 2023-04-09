package com.kongtoon.domain.user.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.user.dto.request.LoginRequest;
import com.kongtoon.domain.user.dto.request.SignupRequest;
import com.kongtoon.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/login")
	public ResponseEntity<Void> login(
			@RequestBody @Valid LoginRequest loginRequest,
			HttpServletRequest httpServletRequest
	) {
		userService.login(loginRequest);

		HttpSession session = httpServletRequest.getSession();
		UserSessionUtil.setLoginMember(session, loginRequest.loginId());

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/signup")
	public ResponseEntity<Void> signup(
			@RequestBody @Valid SignupRequest signupRequest,
			HttpServletRequest httpServletRequest
	) {
		Long savedUserId = userService.signup(signupRequest);

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + savedUserId))
				.build();
	}
}