package com.kongtoon.domain.author.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.author.model.dto.request.AuthorCreateRequest;
import com.kongtoon.domain.author.service.AuthorService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/authors")
@Validated
@RequiredArgsConstructor
public class AuthorController {

	private final AuthorService authorService;

	@LoginCheck(authority = UserAuthority.USER)
	@PostMapping
	public ResponseEntity<Void> createAuthor(
			@RequestBody @Valid AuthorCreateRequest authorCreateRequest,
			@SessionAttribute(name = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedAuthId = authorService.createAuthor(authorCreateRequest, userAuth.loginId());

		return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI() + "/" + savedAuthId)).build();
	}
}
