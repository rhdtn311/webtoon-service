package com.kongtoon.domain.like.controller;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.domain.like.model.dto.response.LikeResponse;
import com.kongtoon.domain.like.service.LikeService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;

@RestController
@Validated
@RequiredArgsConstructor
public class LikeController {

	private final LikeService likeService;

	@LoginCheck(authority = UserAuthority.USER)
	@PostMapping("/episodes/{episodeId}/like")
	public ResponseEntity<LikeResponse> createEpisodeLike(
			@PathVariable @Positive Long episodeId,
			UserAuthDTO userAuth
	) {
		LikeResponse likeResponse = likeService.createEpisodeLike(episodeId, userAuth.loginId());

		return ResponseEntity.ok(likeResponse);
	}

	@LoginCheck(authority = UserAuthority.USER)
	@DeleteMapping("/episodes/{episodeId}/like")
	public ResponseEntity<LikeResponse> deleteEpisodeLike(
			@PathVariable @Positive Long episodeId,
			UserAuthDTO userAuth
	) {
		LikeResponse likeResponse = likeService.deleteEpisodeLike(episodeId, userAuth.loginId());

		return ResponseEntity.ok(likeResponse);
	}
}
