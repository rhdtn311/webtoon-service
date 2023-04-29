package com.kongtoon.domain.like.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.like.model.dto.response.LikeResponse;
import com.kongtoon.domain.like.service.LikeService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LikeController {

	private final LikeService likeService;

	@LoginCheck(authority = UserAuthority.USER)
	@PostMapping("/episodes/{episodeId}/like")
	public ResponseEntity<LikeResponse> createEpisodeLike(
			@PathVariable Long episodeId,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth
	) {
		LikeResponse likeResponse = likeService.createEpisodeLike(episodeId, userAuth.loginId());

		return ResponseEntity.ok(likeResponse);
	}

}
