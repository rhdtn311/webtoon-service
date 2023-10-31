package com.kongtoon.domain.author.service;

import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.model.dto.request.AuthorCreateRequest;
import com.kongtoon.domain.author.model.dto.response.AuthorResponse;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.model.UserAuthority;
import com.kongtoon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class AuthorService {

	private final AuthorRepository authorRepository;
	private final UserRepository userRepository;
	private final ComicRepository comicRepository;
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

		List<Comic> comics = comicRepository.findByAuthorWithThumbnails(author);
		List<Episode> episodes = episodeRepository.findEpisodesByComics(comics);

		Map<Long, String> smallThumbnailUrlsOfComic = getComicIdWithSmallThumbnailUrl(comics);
		Map<Long, Integer> lastEpisodeNumbersOfComic = getComicIdWithLastEpisodeNumber(episodes);

		return AuthorResponse.from(author, comics, smallThumbnailUrlsOfComic, lastEpisodeNumbersOfComic);
	}

	private Author getAuthor(Long authorId) {
		return authorRepository.findById(authorId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));
	}

	private Map<Long, String> getComicIdWithSmallThumbnailUrl(List<Comic> comics) {
		return comics.stream()
				.collect(toMap(Comic::getId, Comic::getSmallTypeThumbnailUrl));
	}

	private Map<Long, Integer> getComicIdWithLastEpisodeNumber(List<Episode> episodes) {
		return episodes.stream()
				.collect(groupingBy(Episode::getComicId, collectLastEpisodeNumber()));
	}

	private Collector<Episode, Object, Integer> collectLastEpisodeNumber() {
		return collectingAndThen(
				maxBy(comparingInt(Episode::getEpisodeNumber)),
				episode -> episode.map(Episode::getEpisodeNumber).orElse(0)
		);
	}
}