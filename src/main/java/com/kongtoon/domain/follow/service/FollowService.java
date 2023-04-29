package com.kongtoon.domain.follow.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.follow.model.Follow;
import com.kongtoon.domain.follow.repository.FollowRepository;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {

	private final FollowRepository followRepository;
	private final ComicRepository comicRepository;
	private final UserRepository userRepository;

	@Transactional
	public Long createFollow(Long comicId, String loginId) {
		User user = getUser(loginId);
		Comic comic = getComic(comicId);

		validateExistsFollow(user, comic);

		Follow follow = createFollow(user, comic);

		followRepository.save(follow);

		return follow.getId();
	}

	@Transactional
	public void deleteFollow(Long comicId, String loginId) {
		User user = getUser(loginId);
		Comic comic = getComic(comicId);

		getFollow(user, comic).ifPresent(followRepository::delete);
	}

	private void validateExistsFollow(User user, Comic comic) {
		if (followRepository.existsByUserAndComic(user, comic)) {
			throw new BusinessException(ErrorCode.DUPLICATE_FOLLOW);
		}
	}

	private Follow createFollow(User user, Comic comic) {
		return new Follow(user, comic);
	}

	private User getUser(String loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private Comic getComic(Long comicId) {
		return comicRepository.findById(comicId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMIC_NOT_FOUND));
	}

	private Optional<Follow> getFollow(User user, Comic comic) {
		return followRepository.findByUserAndComic(user, comic);
	}
}