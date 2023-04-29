package com.kongtoon.domain.star.controller;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.common.session.UserSessionUtil;
import com.kongtoon.domain.star.service.StarService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class StarController {

	private final StarService starService;

	@LoginCheck(authority = UserAuthority.USER)
	@PostMapping("/episodes/{episodeId}/stars")
	public ResponseEntity<Void> createStar(
			@PathVariable Long episodeId,
			@RequestParam @Range(min = 0, max = 10) int score,
			@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedStarId = starService.createStar(episodeId, userAuth.loginId(), score);

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + "/" + savedStarId))
				.build();
	}
}
