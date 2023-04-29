package com.kongtoon.domain.comic.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.comic.model.dto.request.ComicRequest;
import com.kongtoon.domain.comic.service.ComicModifyService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comics")
@RequiredArgsConstructor
public class ComicController {

	private final ComicModifyService comicModifyService;

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PostMapping
	public ResponseEntity<Void> createComic(
			@ModelAttribute @Valid ComicRequest comicRequest,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedComicId = comicModifyService.createComic(comicRequest, userAuth.loginId());

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + savedComicId))
				.build();
	}

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PutMapping("/{comicId}")
	public ResponseEntity<Void> updateComic(
			@PathVariable Long comicId,
			@ModelAttribute @Valid ComicRequest comicRequest,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth
	) {
		comicModifyService.updateComic(comicRequest, comicId, userAuth.loginId());

		return ResponseEntity.noContent().build();
	}
}