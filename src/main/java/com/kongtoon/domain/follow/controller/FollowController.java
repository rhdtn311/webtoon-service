package com.kongtoon.domain.follow.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.follow.service.FollowService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FollowController {

	private final FollowService followService;

	@LoginCheck(authority = UserAuthority.USER)
	@PostMapping("/comics/{comicId}/follow")
	public ResponseEntity<Void> follow(
			@PathVariable Long comicId,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedFollowId = followService.createFollow(comicId, userAuth.loginId());

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + "/" + savedFollowId))
				.build();
	}
}
