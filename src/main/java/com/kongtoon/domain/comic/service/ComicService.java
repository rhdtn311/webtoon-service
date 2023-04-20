package com.kongtoon.domain.comic.service;

import javax.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.kongtoon.common.aws.FileStorage;
import com.kongtoon.common.aws.ImageFileType;
import com.kongtoon.common.aws.event.FileDeleteEvent;
import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.author.repository.AuthorRepository;
import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.comic.entity.Thumbnail;
import com.kongtoon.domain.comic.entity.dto.request.ComicCreateRequest;
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
	public Long createComic(ComicCreateRequest comicCreateRequest, String loginId) {
		User user = getUser(loginId);
		Author author = getAuthor(user);

		Comic comic = comicCreateRequest.toComic(author);
		comicRepository.save(comic);

		comicCreateRequest.thumbnailCreateRequests
				.forEach(thumbnailCreateRequest -> {
					String thumbnailImageUrl = fileStorage.upload(
							thumbnailCreateRequest.getThumbnailImage(),
							ImageFileType.COMIC_THUMBNAIL
					);
					Thumbnail thumbnail = thumbnailCreateRequest.toThumbnail(thumbnailImageUrl, comic);
					thumbnailRepository.save(thumbnail);

					applicationEventPublisher.publishEvent(new FileDeleteEvent(thumbnailImageUrl));
				});

		return comic.getId();
	}

	private Author getAuthor(User user) {
		return authorRepository.findByUser(user)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));
	}

	private User getUser(String loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
