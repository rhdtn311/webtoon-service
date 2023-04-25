package com.kongtoon.domain.episode.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.kongtoon.common.aws.FileStorage;
import com.kongtoon.common.aws.ImageFileType;
import com.kongtoon.common.aws.event.FileDeleteAfterRollbackEvent;
import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.model.EpisodeImage;
import com.kongtoon.domain.episode.model.dto.request.EpisodeRequest;
import com.kongtoon.domain.episode.model.dto.request.EpisodeRequest.EpisodeContentRequest;
import com.kongtoon.domain.episode.repository.EpisodeImageRepository;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EpisodeService {

	private static final int FIRST_EPISODE_NUMBER = 1;

	private final UserRepository userRepository;
	private final EpisodeRepository episodeRepository;
	private final ComicRepository comicRepository;
	private final EpisodeImageRepository episodeImageRepository;

	private final FileStorage fileStorage;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Transactional
	public Long createEpisode(EpisodeRequest episodeRequest, Long comicId, String loginId) {
		User user = getUser(loginId);
		Comic comic = getComicWithAuthor(comicId);

		validateSameAuthor(comic.getAuthor(), user);

		String thumbnailImageUrl = fileStorage.upload(episodeRequest.getThumbnailImage(), ImageFileType.EPISODE_THUMBNAIL);
		applicationEventPublisher.publishEvent(
				new FileDeleteAfterRollbackEvent(thumbnailImageUrl, ImageFileType.EPISODE_THUMBNAIL));

		Integer nextEpisodeNumber = getNextEpisodeNumber(comic);

		Episode episode = episodeRequest.toEpisode(nextEpisodeNumber, thumbnailImageUrl, comic);
		episodeRepository.save(episode);

		List<EpisodeContentRequest> episodeContentRequests = episodeRequest.getEpisodeContentRequests();

		for (EpisodeContentRequest episodeContentRequest : episodeContentRequests) {
			String episodeContentImageUrl = fileStorage.upload(episodeContentRequest.getContentImage(),
					ImageFileType.EPISODE);
			applicationEventPublisher.publishEvent(
					new FileDeleteAfterRollbackEvent(episodeContentImageUrl, ImageFileType.EPISODE));

			EpisodeImage episodeImage = episodeContentRequest.toEpisodeImage(episodeContentImageUrl, episode);
			episodeImageRepository.save(episodeImage);
		}

		return episode.getId();
	}

	private Integer getNextEpisodeNumber(Comic comic) {
		return episodeRepository.findFirstByComicOrderByEpisodeNumberDesc(comic)
				.map(episode -> episode.getEpisodeNumber() + 1)
				.orElse(FIRST_EPISODE_NUMBER);
	}

	private void validateSameAuthor(Author author, User user) {
		if (author.isDifferenceUser(user)) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
	}

	private User getUser(String loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private Comic getComicWithAuthor(Long comicId) {
		return comicRepository.findComicWithAuthor(comicId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMIC_NOT_FOUND));
	}
}
