package com.kongtoon.domain.author.service;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.model.dto.request.AuthorCreateRequest;
import com.kongtoon.domain.author.model.dto.response.AuthorResponse;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Thumbnail;
import com.kongtoon.domain.comic.model.ThumbnailType;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.comic.repository.ThumbnailRepository;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;
import com.kongtoon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthorService {

	private static final int NOT_EXIST_EPISODE_NUMBER = 0;
	private static final String NOT_EXIST_SMALL_THUMBNAIL = "EMPTY";

	private final AuthorRepository authorRepository;
	private final UserRepository userRepository;
	private final ComicRepository comicRepository;
	private final ThumbnailRepository thumbnailRepository;
	private final EpisodeRepository episodeRepository;

	@Transactional
	public Long createAuthor(AuthorCreateRequest authorCreateRequest, LoginId loginId) {
		User user = getUser(loginId);

		validateUserIsAuthor(user);

		Author author = authorCreateRequest.toEntity(user);
		authorRepository.save(author);

		user.setAuthority(UserAuthority.AUTHOR);

		return author.getId();
	}

	private User getUser(LoginId loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private void validateUserIsAuthor(User user) {
		if (user.isAuthor()) {
			throw new BusinessException(ErrorCode.DUPLICATE_APPLY_AUTHOR_AUTHORITY);
		}
	}

	@Transactional(readOnly = true)
	public AuthorResponse getAuthorResponse(Long authorId) {
		Author author = getAuthor(authorId);

		List<Comic> comics = comicRepository.findByAuthor(author);

		Map<Long, String> smallThumbnailUrlsOfComic = getSmallThumbnailUrlsOfComic(comics);
		Map<Long, Integer> lastEpisodeNumbersOfComic = getLastEpisodeNumbersOfComic(comics);

		return AuthorResponse.from(author, comics, smallThumbnailUrlsOfComic, lastEpisodeNumbersOfComic);
	}

	private Author getAuthor(Long authorId) {
		return authorRepository.findById(authorId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));
	}

	private Map<Long, String> getSmallThumbnailUrlsOfComic(List<Comic> comics) {
		Map<Long, String> comicIdsWithSmallThumbnailUrls = new HashMap<>();
		setComicIdsWithDefaultThumbnailUrlsToMap(comics, comicIdsWithSmallThumbnailUrls);

		List<Thumbnail> thumbnails = thumbnailRepository.findByComicInAndThumbnailType(comics, ThumbnailType.SMALL);
		setThumbnailImageUrlsToMap(thumbnails, comicIdsWithSmallThumbnailUrls);

		return comicIdsWithSmallThumbnailUrls;
	}

	private void setComicIdsWithDefaultThumbnailUrlsToMap(List<Comic> comics, Map<Long, String> comicIdsWithSmallThumbnailUrls) {
		comics.forEach(comic ->
				comicIdsWithSmallThumbnailUrls.put(comic.getId(), NOT_EXIST_SMALL_THUMBNAIL)
		);
	}

	private void setThumbnailImageUrlsToMap(List<Thumbnail> thumbnails, Map<Long, String> comicIdsWithSmallThumbnailUrls) {
		thumbnails.forEach(thumbnail ->
				comicIdsWithSmallThumbnailUrls.put(thumbnail.getComic().getId(), thumbnail.getImageUrl())
		);
	}

	private Map<Long, Integer> getLastEpisodeNumbersOfComic(List<Comic> comics) {
		Map<Long, Integer> comicIdsWithLastEpisodeNumbers = new HashMap<>();
		setComicIdsWithDefaultEpisodeNumberToMap(comics, comicIdsWithLastEpisodeNumbers);

		List<Episode> episodes = episodeRepository.findRecentlyEpisodesByComics(comics);
		setEpisodeNumbersToMap(episodes, comicIdsWithLastEpisodeNumbers);

		return comicIdsWithLastEpisodeNumbers;
	}

	private void setComicIdsWithDefaultEpisodeNumberToMap(List<Comic> comics, Map<Long, Integer> comicIdsWithLastEpisodeNumbers) {
		comics.forEach(comic ->
				comicIdsWithLastEpisodeNumbers.put(comic.getId(), NOT_EXIST_EPISODE_NUMBER)
		);
	}

	private void setEpisodeNumbersToMap(List<Episode> episodes, Map<Long, Integer> comicIdsWithLastEpisodeNumbers) {
		episodes.forEach(episode ->
				comicIdsWithLastEpisodeNumbers.put(episode.getComic().getId(), episode.getEpisodeNumber())
		);
	}
}