package com.kongtoon.domain.comic.controller;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.dto.request.ComicRequest;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByNewResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByRealtimeRankingResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByViewRecentResponse;
import com.kongtoon.domain.comic.service.ComicModifyService;
import com.kongtoon.domain.comic.service.ComicReadService;
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
import java.util.List;

@RestController
@RequestMapping("/comics")
@Validated
@RequiredArgsConstructor
public class ComicController {

	private final ComicReadService comicReadService;
	private final ComicModifyService comicModifyService;

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PostMapping
	public ResponseEntity<Void> createComic(
			@ModelAttribute @Valid ComicRequest comicRequest,
			UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedComicId = comicModifyService.createComic(comicRequest, userAuth.loginId());

		return ResponseEntity
				.created(URI.create(httpServletRequest.getRequestURI() + "/" + savedComicId))
				.build();
	}

	@LoginCheck(authority = UserAuthority.AUTHOR)
	@PutMapping("/{comicId}")
	public ResponseEntity<Void> updateComic(
			@PathVariable @Positive Long comicId,
			@ModelAttribute @Valid ComicRequest comicRequest,
			UserAuthDTO userAuth
	) {
		comicModifyService.updateComic(comicRequest, comicId, userAuth.loginId());

		return ResponseEntity.noContent().build();
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/by-genre")
	public ResponseEntity<List<ComicByGenreResponse>> getComicsByGenre(@RequestParam Genre genre) {

		List<ComicByGenreResponse> comicByGenreResponses = comicReadService.getComicsByGenre(genre);

		return ResponseEntity.ok(comicByGenreResponses);
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/recent")
	public ResponseEntity<List<ComicByViewRecentResponse>> getComicsByViewRecent(UserAuthDTO userAuth) {
		List<ComicByViewRecentResponse> comicsByViewRecentResponses = comicReadService.getComicsByViewRecent(
				userAuth.userId());

		return ResponseEntity.ok(comicsByViewRecentResponses);
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/new")
	public ResponseEntity<List<ComicByNewResponse>> getComicsByNew() {
		List<ComicByNewResponse> comicsByViewRecentResponses = comicReadService.getComicsByNew();

		return ResponseEntity.ok(comicsByViewRecentResponses);
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/real-time-ranking")
	public ResponseEntity<List<ComicByRealtimeRankingResponse>> getComicsByRealtimeRanking() {
		List<ComicByRealtimeRankingResponse> comicsByRealtimeRankingResponses = comicReadService.getComicsByRealtimeRanking();

		return ResponseEntity.ok(comicsByRealtimeRankingResponses);
	}
}