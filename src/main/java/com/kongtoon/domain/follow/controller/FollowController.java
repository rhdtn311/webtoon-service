package com.kongtoon.domain.follow.controller;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.domain.follow.model.dto.response.FollowResponse;
import com.kongtoon.domain.follow.service.FollowService;
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
public class FollowController {

	private final FollowService followService;

	@LoginCheck(authority = UserAuthority.USER)
	@PostMapping("/comics/{comicId}/follow")
	public ResponseEntity<FollowResponse> follow(
			@PathVariable @Positive Long comicId,
			UserAuthDTO userAuth
	) {
		FollowResponse followResponse = followService.createFollow(comicId, userAuth.loginId());

		return ResponseEntity.ok(followResponse);
	}

	@LoginCheck(authority = UserAuthority.USER)
	@DeleteMapping("/comics/{comicId}/follow")
	public ResponseEntity<FollowResponse> unFollow(
			@PathVariable @Positive Long comicId,
			UserAuthDTO userAuth
	) {
		FollowResponse followResponse = followService.deleteFollow(comicId, userAuth.loginId());

		return ResponseEntity.ok(followResponse);
	}
}
