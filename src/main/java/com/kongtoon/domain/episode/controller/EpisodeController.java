package com.kongtoon.domain.episode.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.episode.model.dto.request.EpisodeRequest;
import com.kongtoon.domain.episode.model.dto.response.EpisodeListResponses;
import com.kongtoon.domain.episode.model.dto.response.EpisodeResponse;
import com.kongtoon.domain.episode.service.EpisodeModifyService;
import com.kongtoon.domain.episode.service.EpisodeReadService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EpisodeController {

	private final EpisodeModifyService episodeModifyService;
	private final EpisodeReadService episodeReadService;

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PostMapping("/episodes")
	public ResponseEntity<Void> createEpisode(
			@RequestParam Long comicId,
			@ModelAttribute @Valid EpisodeRequest episodeRequest,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedEpisodeId = episodeModifyService.createEpisodeAndEpisodeImage(episodeRequest, comicId,
				userAuth.loginId());

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + "/" + savedEpisodeId))
				.build();
	}

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PutMapping("/episodes/{episodeId}")
	public ResponseEntity<Void> updateEpisode(
			@PathVariable Long episodeId,
			@ModelAttribute @Valid EpisodeRequest episodeRequest,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth
	) {
		episodeModifyService.updateEpisode(episodeRequest, episodeId, userAuth.loginId());

		return ResponseEntity.noContent().build();
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/comics/{comicId}/episodes")
	public ResponseEntity<EpisodeListResponses> getEpisodes(
			@PathVariable Long comicId,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth
	) {
		EpisodeListResponses episodes = episodeReadService.getEpisodes(comicId, userAuth.loginId());
		return ResponseEntity.ok(episodes);
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/episodes/{episodeId}")
	public ResponseEntity<EpisodeResponse> getEpisode(
			@PathVariable Long episodeId
	) {
		EpisodeResponse episodeResponse = episodeReadService.getEpisodeResponse(episodeId);

		return ResponseEntity.ok(episodeResponse);
	}

}
