package com.kongtoon.domain.episode.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.episode.model.dto.request.EpisodeRequest;
import com.kongtoon.domain.episode.service.EpisodeService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/episodes")
@RequiredArgsConstructor
public class EpisodeController {

	private final EpisodeService episodeService;

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PostMapping
	public ResponseEntity<Void> createEpisode(
			@RequestParam Long comicId,
			@ModelAttribute @Valid EpisodeRequest episodeRequest,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedEpisodeId = episodeService.createEpisodeAndEpisodeImage(episodeRequest, comicId, userAuth.loginId());

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + "/" + savedEpisodeId))
				.build();
	}

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PutMapping("/{episodeId}")
	public ResponseEntity<Void> updateEpisode(
			@PathVariable Long episodeId,
			@ModelAttribute @Valid EpisodeRequest episodeRequest,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth
	) {
		episodeService.updateEpisode(episodeRequest, episodeId, userAuth.loginId());

		return ResponseEntity.noContent().build();
	}
}
