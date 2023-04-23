package com.kongtoon.domain.comic.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.comic.entity.dto.request.ComicRequest;
import com.kongtoon.domain.comic.service.ComicService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comics")
@RequiredArgsConstructor
public class ComicController {

	private final ComicService comicService;

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PostMapping
	public ResponseEntity<Void> createComic(
			@ModelAttribute @Valid ComicRequest comicRequest,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedComicId = comicService.createComic(comicRequest, userAuth.loginId());

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + savedComicId))
				.build();
	}
}
