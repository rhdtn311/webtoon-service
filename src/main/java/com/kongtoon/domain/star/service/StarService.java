package com.kongtoon.domain.star.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.star.model.Star;
import com.kongtoon.domain.star.repository.StarRepository;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StarService {

	private final StarRepository starRepository;
	private final UserRepository userRepository;
	private final EpisodeRepository episodeRepository;

	@Transactional
	public Long createStar(Long episodeId, String loginId, int score) {
		User user = getUser(loginId);
		Episode episode = getEpisode(episodeId);

		Star star = getStarIfPresent(user, episode)
				.orElse(createStar(score, user, episode));

		star.updateScore(score);
		starRepository.save(star);

		return star.getId();
	}

	private Star createStar(int score, User user, Episode episode) {
		return new Star(score, user, episode);
	}

	private Optional<Star> getStarIfPresent(User user, Episode episode) {
		return starRepository.findByUserAndEpisode(user, episode);
	}

	private User getUser(String loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private Episode getEpisode(Long episodeId) {
		return episodeRepository.findById(episodeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND));
	}
}
