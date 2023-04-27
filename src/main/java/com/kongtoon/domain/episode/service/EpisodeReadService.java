package com.kongtoon.domain.episode.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.comic.entity.Thumbnail;
import com.kongtoon.domain.comic.entity.ThumbnailType;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.comic.repository.ThumbnailRepository;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.model.dto.response.EpisodeListResponses;
import com.kongtoon.domain.episode.model.dto.response.EpisodeListResponses.ComicInfo;
import com.kongtoon.domain.episode.model.dto.response.EpisodeListResponses.EpisodeListResponse;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import com.kongtoon.domain.view.model.View;
import com.kongtoon.domain.view.repository.ViewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EpisodeReadService {

	private static final String EMPTY_MAIN_THUMBNAIL_URL = "";

	private final EpisodeRepository episodeRepository;
	private final ComicRepository comicRepository;
	private final UserRepository userRepository;
	private final ViewRepository viewRepository;
	private final ThumbnailRepository thumbnailRepository;

	@Transactional(readOnly = true)
	public EpisodeListResponses getEpisodes(Long comicId, String loginId) {
		List<Episode> episodes = episodeRepository.findByComicIdWithComicAndAuthor(comicId);
		Comic comic = getComic(comicId);
		User user = getUser(loginId);

		List<EpisodeListResponse> episodesInfos = getEpisodeListResponses(episodes, user);

		String mainThumbnailUrl = getMainThumbnailUrl(comic);
		ComicInfo comicInfo = ComicInfo.from(comic, mainThumbnailUrl);

		return new EpisodeListResponses(
				comicInfo,
				episodesInfos
		);
	}

	private List<EpisodeListResponse> getEpisodeListResponses(List<Episode> episodes, User user) {
		List<View> views = viewRepository.findByUserAndEpisodeIn(user, episodes);

		return episodes.stream()
				.map(episode -> EpisodeListResponse.from(episode, isRead(views, episode)))
				.toList();
	}

	private boolean isRead(List<View> views, Episode episode) {
		return views.stream()
				.anyMatch(view -> episode.isSame(view.getEpisode()));
	}

	private Comic getComic(Long comicId) {
		return comicRepository.findById(comicId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMIC_NOT_FOUND));
	}

	private User getUser(String loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private String getMainThumbnailUrl(Comic comic) {
		return thumbnailRepository.findByComicAndThumbnailType(comic, ThumbnailType.MAIN)
				.map(Thumbnail::getImageUrl)
				.orElse(EMPTY_MAIN_THUMBNAIL_URL);
	}
}