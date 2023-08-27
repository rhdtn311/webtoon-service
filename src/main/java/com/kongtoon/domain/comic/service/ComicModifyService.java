package com.kongtoon.domain.comic.service;

import com.kongtoon.common.aws.FileStorage;
import com.kongtoon.common.aws.ImageFileType;
import com.kongtoon.common.aws.event.FileDeleteAfterCommitEvent;
import com.kongtoon.common.aws.event.FileDeleteAfterRollbackEvent;
import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Thumbnail;
import com.kongtoon.domain.comic.model.dto.request.ComicRequest;
import com.kongtoon.domain.comic.model.dto.request.ComicRequest.ThumbnailRequest;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.comic.repository.ThumbnailRepository;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ComicModifyService {

	private final ComicRepository comicRepository;
	private final UserRepository userRepository;
	private final AuthorRepository authorRepository;
	private final ThumbnailRepository thumbnailRepository;

	private final FileStorage fileStorage;

	private final ApplicationEventPublisher applicationEventPublisher;

	@Transactional
	public Long createComic(ComicRequest comicRequest, LoginId loginId) {
		User user = getUser(loginId);
		Author author = getAuthor(user);

		Comic comic = comicRequest.toComic(author);
		comicRepository.save(comic);

		comicRequest.getThumbnailRequests().forEach(thumbnailRequest -> {
			String thumbnailImageUrl = uploadThumbnailToFileStorage(thumbnailRequest);
			Thumbnail thumbnail = thumbnailRequest.toThumbnail(thumbnailImageUrl, comic);
			thumbnailRepository.save(thumbnail);

			callThumbnailImageDeleteAfterRollbackEvent(thumbnailImageUrl);
		});

		return comic.getId();
	}

	@Transactional
	public void updateComic(ComicRequest comicRequest, Long comicId, LoginId loginId) {
		User user = getUser(loginId);
		Comic comic = getComicWithAuthor(comicId);
		Author author = comic.getAuthor();

		validateSameAuthor(author, user);

		comic.update(
				comicRequest.getComicName(),
				comicRequest.getGenre(),
				comicRequest.getSummary(),
				comicRequest.getPublishDayOfWeek()
		);

		List<Thumbnail> thumbnails = thumbnailRepository.findByComic(comic);
		List<ThumbnailRequest> thumbnailRequests = comicRequest.getThumbnailRequests();

		updateThumbnailUrls(thumbnailRequests, thumbnails, comic);
	}

	private void updateThumbnailUrls(List<ThumbnailRequest> thumbnailRequests, List<Thumbnail> thumbnails, Comic comic) {
		thumbnailRequests.forEach(thumbnailRequest -> {
					String thumbnailImageUrl = uploadThumbnailToFileStorage(thumbnailRequest);
					callThumbnailImageDeleteAfterRollbackEvent(thumbnailImageUrl);

					updateOrSaveThumbnail(thumbnails, comic, thumbnailRequest, thumbnailImageUrl);
				}
		);
	}

	private void updateOrSaveThumbnail(List<Thumbnail> thumbnails, Comic comic, ThumbnailRequest thumbnailRequest, String thumbnailImageUrl) {
		thumbnails.stream()
				.filter(thumbnail -> thumbnail.getThumbnailType().isSameType(thumbnailRequest.getThumbnailType()))
				.findFirst()
				.ifPresentOrElse(
						updateThumbnail(thumbnailImageUrl),
						saveThumbnail(comic, thumbnailRequest, thumbnailImageUrl)
				);
	}

	private Consumer<Thumbnail> updateThumbnail(String thumbnailImageUrl) {
		return thumbnail -> {
			callThumbnailImageDeleteAfterCommitEvent(thumbnail);
			thumbnail.update(thumbnail.getThumbnailType(), thumbnailImageUrl);
		};
	}

	private Runnable saveThumbnail(Comic comic, ThumbnailRequest thumbnailRequest, String thumbnailImageUrl) {
		return () -> {
			Thumbnail thumbnail = thumbnailRequest.toThumbnail(thumbnailImageUrl, comic);
			thumbnailRepository.save(thumbnail);
		};
	}

	private String uploadThumbnailToFileStorage(ThumbnailRequest thumbnailRequest) {
		return fileStorage.upload(
				thumbnailRequest.getThumbnailImage(),
				ImageFileType.COMIC_THUMBNAIL
		);
	}

	private void callThumbnailImageDeleteAfterRollbackEvent(String thumbnailImageUrl) {
		applicationEventPublisher.publishEvent(
				new FileDeleteAfterRollbackEvent(thumbnailImageUrl, ImageFileType.COMIC_THUMBNAIL)
		);
	}

	private void callThumbnailImageDeleteAfterCommitEvent(Thumbnail thumbnail) {
		applicationEventPublisher.publishEvent(
				new FileDeleteAfterCommitEvent(thumbnail.getImageUrl(), ImageFileType.COMIC_THUMBNAIL)
		);
	}

	private void validateSameAuthor(Author author, User user) {
		if (author.isDifferenceUser(user)) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
	}

	private Author getAuthor(User user) {
		return authorRepository.findByUser(user)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));
	}

	private User getUser(LoginId loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private Comic getComicWithAuthor(Long comicId) {
		return comicRepository.findComicWithAuthor(comicId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMIC_NOT_FOUND));
	}
}
