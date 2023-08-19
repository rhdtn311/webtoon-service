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

	@Transactional(readOnly = true)
	public AuthorResponse getAuthor(Long authorId) {
		Author author = authorRepository.findById(authorId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));

		List<Comic> comics = comicRepository.findByAuthor(author);

		Map<Long, String> smallThumbnailUrlsOfComic = getSmallThumbnailUrlsOfComic(comics);
		Map<Long, Integer> lastEpisodeNumbersOfComic = getLastEpisodeNumbersOfComic(comics);

		return AuthorResponse.from(author, comics, smallThumbnailUrlsOfComic, lastEpisodeNumbersOfComic);
	}

	private Map<Long, String> getSmallThumbnailUrlsOfComic(List<Comic> comics) {

		Map<Long, String> smallThumbnailUrls = new HashMap<>();
		comics.forEach(comic ->
				smallThumbnailUrls.put(comic.getId(), NOT_EXIST_SMALL_THUMBNAIL)
		);

		List<Thumbnail> thumbnails = thumbnailRepository.findByComicInAndThumbnailType(comics, ThumbnailType.SMALL);

		thumbnails.forEach(thumbnail ->
				smallThumbnailUrls.put(thumbnail.getComic().getId(), thumbnail.getImageUrl())
		);

		return smallThumbnailUrls;
	}

	private Map<Long, Integer> getLastEpisodeNumbersOfComic(List<Comic> comics) {

		Map<Long, Integer> lastEpisodeNumbersOfComic = new HashMap<>();
		comics.forEach(comic ->
				lastEpisodeNumbersOfComic.put(comic.getId(), NOT_EXIST_EPISODE_NUMBER)
		);

		List<Episode> episodes = episodeRepository.findRecentlyEpisodesByComics(comics);

		episodes.forEach(episode ->
				lastEpisodeNumbersOfComic.put(episode.getComic().getId(), episode.getEpisodeNumber())
		);

		return lastEpisodeNumbersOfComic;
	}
}