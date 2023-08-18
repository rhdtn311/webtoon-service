package com.kongtoon.domain.star.controller;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.domain.star.service.StarService;
import com.kongtoon.domain.user.dto.UserAuthDTO;
import com.kongtoon.domain.user.model.UserAuthority;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import java.net.URI;

@RestController
@Validated
@RequiredArgsConstructor
public class StarController {

	private final StarService starService;

	@LoginCheck(authority = UserAuthority.USER)
	@PostMapping("/episodes/{episodeId}/stars")
	public ResponseEntity<Void> createStar(
			@PathVariable @Positive Long episodeId,
			@RequestParam @Range(min = 0, max = 10) int score,
			UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedStarId = starService.createStar(episodeId, userAuth.loginId(), score);

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + "/" + savedStarId))
				.build();
	}
}
