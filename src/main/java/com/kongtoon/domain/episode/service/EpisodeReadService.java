package com.kongtoon.domain.episode.service;

import static com.kongtoon.domain.episode.model.dto.response.EpisodeDetailResponse.*;
import static com.kongtoon.domain.episode.model.dto.response.EpisodeResponse.*;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Thumbnail;
import com.kongtoon.domain.comic.model.ThumbnailType;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.comic.repository.ThumbnailRepository;
import com.kongtoon.domain.comment.repository.CommentRepository;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.model.EpisodeImage;
import com.kongtoon.domain.episode.model.dto.response.EpisodeDetailResponse;
import com.kongtoon.domain.episode.model.dto.response.EpisodeListResponses;
import com.kongtoon.domain.episode.model.dto.response.EpisodeListResponses.ComicInfo;
import com.kongtoon.domain.episode.model.dto.response.EpisodeListResponses.EpisodeListResponse;
import com.kongtoon.domain.episode.model.dto.response.EpisodeResponse;
import com.kongtoon.domain.episode.repository.EpisodeImageRepository;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.follow.repository.FollowRepository;
import com.kongtoon.domain.like.model.LikeType;
import com.kongtoon.domain.like.repository.LikeRepository;
import com.kongtoon.domain.star.repository.StarRepository;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import com.kongtoon.domain.view.model.View;
import com.kongtoon.domain.view.repository.ViewRepository;
import com.kongtoon.domain.view.service.event.EpisodeViewedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EpisodeReadService {

	private static final String EMPTY_MAIN_THUMBNAIL_URL = "";
	private static final Double DEFAULT_STAR_AVG = 0D;

	private final EpisodeRepository episodeRepository;
	private final ComicRepository comicRepository;
	private final UserRepository userRepository;
	private final ViewRepository viewRepository;
	private final ThumbnailRepository thumbnailRepository;
	private final EpisodeImageRepository episodeImageRepository;
	private final LikeRepository likeRepository;
	private final FollowRepository followRepository;
	private final StarRepository starRepository;
	private final CommentRepository commentRepository;

	private final ApplicationEventPublisher applicationEventPublisher;

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

	@Transactional(readOnly = true)
	public EpisodeResponse getEpisodeResponse(Long episodeId) {
		Episode episode = getEpisode(episodeId);
		List<EpisodeImage> episodeImages = getEpisodeImages(episode);
		List<EpisodeImageResponse> episodeImageResponses = EpisodeImageResponse.toEpisodeImageResponses(episodeImages);

		return EpisodeResponse.from(episode, episodeImageResponses);
	}

	@Transactional(readOnly = true)
	public EpisodeDetailResponse getEpisodeDetailResponse(Long episodeId, String loginId) {
		User user = getUser(loginId);
		Episode episode = getEpisodeWithComic(episodeId);
		Comic comic = episode.getComic();

		LikeResponse likeResponse = getLikeInfo(episodeId, user);
		FollowResponse followResponse = getFollowInfo(user, comic);
		StarResponse starResponse = getStarInfo(user, episode);
		int commentCount = commentRepository.countByEpisode(episode);

		applicationEventPublisher.publishEvent(new EpisodeViewedEvent(user, episode));

		return EpisodeDetailResponse.from(commentCount, likeResponse, followResponse, starResponse);
	}

	private LikeResponse getLikeInfo(Long episodeId, User user) {
		boolean isLike = likeRepository.existsByUserAndLikeTypeAndReferenceId(user, LikeType.EPISODE, episodeId);
		int likeCount = likeRepository.countByLikeTypeAndReferenceId(LikeType.EPISODE, episodeId);

		return LikeResponse.from(likeCount, isLike);
	}

	private FollowResponse getFollowInfo(User user, Comic comic) {
		boolean isFollow = followRepository.existsByUserAndComic(user, comic);
		int followCount = followRepository.countByComic(comic);

		return FollowResponse.from(followCount, isFollow);
	}

	private StarResponse getStarInfo(User user, Episode episode) {
		boolean isStar = starRepository.existsByUserAndEpisode(user, episode);
		int starCount = starRepository.countByEpisode(episode);
		double scoreAverage = starRepository.findAvgScoreByEpisode(episode)
				.orElse(DEFAULT_STAR_AVG);

		return StarResponse.from(starCount, scoreAverage, isStar);
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

	private Episode getEpisode(Long episodeId) {
		return episodeRepository.findById(episodeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND));
	}

	private List<EpisodeImage> getEpisodeImages(Episode episode) {
		return episodeImageRepository.findByEpisode(episode);
	}

	private Episode getEpisodeWithComic(Long episodeId) {
		return episodeRepository.findByIdWithComic(episodeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND));
	}
}
