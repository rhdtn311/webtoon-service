package com.kongtoon.domain.author.controller;

import com.kongtoon.common.security.annotation.LoginCheck;
import com.kongtoon.domain.author.model.dto.request.AuthorCreateRequest;
import com.kongtoon.domain.author.model.dto.response.AuthorResponse;
import com.kongtoon.domain.author.service.AuthorService;
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
@RequestMapping("/authors")
@Validated
@RequiredArgsConstructor
public class AuthorController {

	private final AuthorService authorService;

	@LoginCheck(authority = UserAuthority.USER)
	@PostMapping
	public ResponseEntity<Void> createAuthor(
			@RequestBody @Valid AuthorCreateRequest authorCreateRequest,
			UserAuthDTO userAuth,
			HttpServletRequest httpServletRequest
	) {
		Long savedAuthId = authorService.createAuthor(authorCreateRequest, userAuth.loginId());

		return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI() + "/" + savedAuthId)).build();
	}

	@LoginCheck(authority = UserAuthority.USER)
	@GetMapping("/{authorId}")
	public ResponseEntity<AuthorResponse> getAuthor(@PathVariable @Positive Long authorId) {
		AuthorResponse authorResponse = authorService.getAuthorResponse(authorId);

		return ResponseEntity.ok(authorResponse);
	}
}
