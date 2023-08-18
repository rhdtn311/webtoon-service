package com.kongtoon.domain.episode.controller;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.domain.episode.model.dto.request.EpisodeRequest;
import com.kongtoon.domain.episode.model.dto.response.EpisodeDetailResponse;
import com.kongtoon.domain.episode.model.dto.response.EpisodeListResponses;
import com.kongtoon.domain.episode.model.dto.response.EpisodeResponse;
import com.kongtoon.domain.episode.service.EpisodeModifyService;
import com.kongtoon.domain.episode.service.EpisodeReadService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;

@RestController
@Validated
@RequiredArgsConstructor
public class EpisodeController {

	private final EpisodeModifyService episodeModifyService;
	private final EpisodeReadService episodeReadService;

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PostMapping("/episodes")
	public ResponseEntity<Void> createEpisode(
			@RequestParam @Positive Long comicId,
			@ModelAttribute @Valid EpisodeRequest episodeRequest,
			UserAuthDTO userAuth,
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
			@PathVariable @Positive Long episodeId,
			@ModelAttribute @Valid EpisodeRequest episodeRequest,
			UserAuthDTO userAuth
	) {
		episodeModifyService.updateEpisode(episodeRequest, episodeId, userAuth.loginId());

		return ResponseEntity.noContent().build();
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/comics/{comicId}/episodes")
	public ResponseEntity<EpisodeListResponses> getEpisodes(
			@PathVariable @Positive Long comicId,
			UserAuthDTO userAuth
	) {
		EpisodeListResponses episodes = episodeReadService.getEpisodes(comicId, userAuth.loginId());
		return ResponseEntity.ok(episodes);
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/episodes/{episodeId}")
	public ResponseEntity<EpisodeResponse> getEpisode(
			@PathVariable @Positive Long episodeId
	) {
		EpisodeResponse episodeResponse = episodeReadService.getEpisodeResponse(episodeId);

		return ResponseEntity.ok(episodeResponse);
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/episodes/{episodeId}/detail")
	public ResponseEntity<EpisodeDetailResponse> getEpisodeDetail(
			@PathVariable @Positive Long episodeId,
			UserAuthDTO userAuth
	) {
		EpisodeDetailResponse episodeDetailResponse = episodeReadService.getEpisodeDetailResponse(
				episodeId,
				userAuth.loginId()
		);

		return ResponseEntity.ok(episodeDetailResponse);
	}
}
