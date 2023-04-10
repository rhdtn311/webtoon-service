package com.kongtoon.domain.author.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.model.dto.request.AuthorCreateRequest;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;
import com.kongtoon.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorService {

	private final AuthorRepository authorRepository;
	private final UserRepository userRepository;

	@Transactional
	public Long createAuthor(AuthorCreateRequest authorCreateRequest, String loginId) {
		User user = userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		validateUserIsAuthor(user);

		Author author = authorCreateRequest.toEntity(user);
		authorRepository.save(author);

		user.setAuthority(UserAuthority.AUTHOR);

		return author.getId();
	}

	private void validateUserIsAuthor(User user) {
		if (user.isAuthor()) {
			throw new BusinessException(ErrorCode.DUPLICATE_APPLY_AUTHOR_AUTHORITY);
		}
	}
}