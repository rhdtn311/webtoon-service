package com.kongtoon.domain.comic.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.kongtoon.common.aws.FileStorage;
import com.kongtoon.common.aws.ImageFileType;
import com.kongtoon.common.aws.event.FileDeleteAfterCommitEvent;
import com.kongtoon.common.aws.event.FileDeleteAfterRollbackEvent;
import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.comic.entity.Thumbnail;
import com.kongtoon.domain.comic.entity.dto.request.ComicRequest;
import com.kongtoon.domain.comic.entity.dto.request.ComicRequest.ThumbnailRequest;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.comic.repository.ThumbnailRepository;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComicService {

	private final ComicRepository comicRepository;
	private final UserRepository userRepository;
	private final AuthorRepository authorRepository;
	private final ThumbnailRepository thumbnailRepository;

	private final FileStorage fileStorage;

	private final ApplicationEventPublisher applicationEventPublisher;

	@Transactional
	public Long createComic(ComicRequest comicRequest, String loginId) {
		User user = getUser(loginId);
		Author author = getAuthor(user);

		Comic comic = comicRequest.toComic(author);
		comicRepository.save(comic);

		comicRequest.getThumbnailRequests()
				.forEach(thumbnailRequest -> {
					String thumbnailImageUrl = fileStorage.upload(
							thumbnailRequest.getThumbnailImage(),
							ImageFileType.COMIC_THUMBNAIL
					);
					Thumbnail thumbnail = thumbnailRequest.toThumbnail(thumbnailImageUrl, comic);
					thumbnailRepository.save(thumbnail);

					applicationEventPublisher.publishEvent(
							new FileDeleteAfterRollbackEvent(thumbnailImageUrl, ImageFileType.COMIC_THUMBNAIL));
				});

		return comic.getId();
	}

	@Transactional
	public void updateComic(ComicRequest comicRequest, Long comicId, String loginId) {
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

		for (ThumbnailRequest thumbnailRequest : thumbnailRequests) {

			String thumbnailImageUrl = fileStorage.upload(thumbnailRequest.getThumbnailImage(),
					ImageFileType.COMIC_THUMBNAIL);
			applicationEventPublisher.publishEvent(
					new FileDeleteAfterRollbackEvent(thumbnailImageUrl, ImageFileType.COMIC_THUMBNAIL));

			thumbnails.stream()
					.filter(thumbnail -> thumbnail.getThumbnailType().isSameType(thumbnailRequest.getThumbnailType()))
					.findFirst()
					.ifPresentOrElse(
							thumbnail -> {
								applicationEventPublisher.publishEvent(
										new FileDeleteAfterCommitEvent(thumbnail.getImageUrl(), ImageFileType.COMIC_THUMBNAIL));
								thumbnail.update(thumbnail.getThumbnailType(), thumbnailImageUrl);
							},
							() -> {
								Thumbnail thumbnail = thumbnailRequest.toThumbnail(thumbnailImageUrl, comic);
								thumbnailRepository.save(thumbnail);
							});
		}
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

	private User getUser(String loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private Comic getComicWithAuthor(Long comicId) {
		return comicRepository.findComicWithAuthor(comicId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMIC_NOT_FOUND));
	}
}
