package com.kongtoon.domain.like.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.like.model.Like;
import com.kongtoon.domain.like.model.LikeType;
import com.kongtoon.domain.like.model.dto.response.LikeResponse;
import com.kongtoon.domain.like.repository.LikeRepository;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;
	private final UserRepository userRepository;
	private final EpisodeRepository episodeRepository;

	@Transactional
	public LikeResponse createEpisodeLike(Long episodeId, String loginId) {
		User user = getUser(loginId);

		verityExistsEpisode(episodeId);
		verifyExistsLike(user, episodeId);

		Like like = new Like(LikeType.EPISODE, episodeId, user);
		likeRepository.save(like);

		int likeCount = getLikeCountByEpisodeId(episodeId);

		return LikeResponse.from(likeCount, true);
	}

	@Transactional
	public LikeResponse deleteEpisodeLike(Long episodeId, String loginId) {
		User user = getUser(loginId);

		likeRepository.findByUserAndLikeTypeAndReferenceId(user, LikeType.EPISODE, episodeId)
				.ifPresent(likeRepository::delete);

		int likeCount = getLikeCountByEpisodeId(episodeId);

		return LikeResponse.from(likeCount, false);
	}

	private void verityExistsEpisode(Long episodeId) {
		if (!episodeRepository.existsById(episodeId)) {
			throw new BusinessException(ErrorCode.EPISODE_NOT_FOUND);
		}
	}

	private int getLikeCountByEpisodeId(Long episodeId) {
		return likeRepository.countByLikeTypeAndReferenceId(LikeType.EPISODE, episodeId);
	}

	private void verifyExistsLike(User user, Long episodeId) {
		if (likeRepository.existsByUserAndLikeTypeAndReferenceId(user, LikeType.EPISODE, episodeId)) {
			throw new BusinessException(ErrorCode.DUPLICATE_LIKE);
		}
	}

	private User getUser(String loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
