package com.kongtoon.domain.episode.service;

import com.kongtoon.common.aws.FileStorage;
import com.kongtoon.common.aws.FileType;
import com.kongtoon.common.aws.ImageFileType;
import com.kongtoon.common.aws.event.FileDeleteAfterCommitEvent;
import com.kongtoon.common.exception.BusinessException;
import com.kongtoon.common.exception.ErrorCode;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.model.EpisodeImage;
import com.kongtoon.domain.episode.model.dto.request.EpisodeRequest;
import com.kongtoon.domain.episode.model.dto.request.EpisodeRequest.EpisodeContentRequest;
import com.kongtoon.domain.episode.repository.EpisodeImageRepository;
import com.kongtoon.domain.episode.repository.EpisodeRepository;
import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EpisodeModifyService {

	private static final int FIRST_EPISODE_NUMBER = 1;

	private final UserRepository userRepository;
	private final EpisodeRepository episodeRepository;
	private final ComicRepository comicRepository;
	private final EpisodeImageRepository episodeImageRepository;

	private final FileStorage fileStorage;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Transactional
	public Long createEpisodeAndEpisodeImage(EpisodeRequest episodeRequest, Long comicId, LoginId loginId) {
		User user = getUser(loginId);
		Comic comic = getComicWithAuthor(comicId);

		validateSameAuthor(comic.getAuthor(), user);

		Episode episode = createEpisode(episodeRequest, comic);

		createEpisodeImages(episodeRequest, episode);

		return episode.getId();
	}

	private Episode createEpisode(EpisodeRequest episodeRequest, Comic comic) {
		String thumbnailImageUrl = fileStorage.upload(episodeRequest.getThumbnailImage(), ImageFileType.EPISODE_THUMBNAIL);

		callDeleteFileAfterRollbackEvent(thumbnailImageUrl, ImageFileType.EPISODE_THUMBNAIL);

		Integer nextEpisodeNumber = getNextEpisodeNumber(comic);

		Episode episode = episodeRequest.toEpisode(nextEpisodeNumber, thumbnailImageUrl, comic);
		episodeRepository.save(episode);
		return episode;
	}

	private void createEpisodeImages(EpisodeRequest episodeRequest, Episode episode) {
		List<EpisodeContentRequest> episodeContentRequests = episodeRequest.getEpisodeContentRequests();

		episodeContentRequests.forEach(episodeContentRequest -> {
					String episodeContentImageUrl = fileStorage.upload(episodeContentRequest.getContentImage(), ImageFileType.EPISODE);
					callDeleteFileAfterRollbackEvent(episodeContentImageUrl, ImageFileType.EPISODE);
					EpisodeImage episodeImage = episodeContentRequest.toEpisodeImage(episodeContentImageUrl, episode);
					episodeImageRepository.save(episodeImage);
				}
		);
	}

	@Transactional
	public void updateEpisode(EpisodeRequest episodeRequest, Long episodeId, LoginId loginId) {
		User user = getUser(loginId);
		Episode episode = getEpisodeWithComicAndAuthor(episodeId);
		Comic comic = episode.getComic();
		Author author = comic.getAuthor();

		validateSameAuthor(author, user);

		updateEpisodeThumbnail(episodeRequest, episode);

		List<EpisodeImage> savedEpisodeImages = episodeImageRepository.findByEpisode(episode);
		List<EpisodeContentRequest> episodeContentRequests = episodeRequest.getEpisodeContentRequests();

		updateEpisodeContentImages(savedEpisodeImages, episodeContentRequests);

		createEpisodeContentImages(episode, savedEpisodeImages, episodeContentRequests);

		deleteEpisodeContentImages(savedEpisodeImages, episodeContentRequests);
	}

	private void updateEpisodeThumbnail(EpisodeRequest episodeRequest, Episode episode) {
		callDeleteFileAfterCommitEvent(episode.getThumbnailUrl(), ImageFileType.EPISODE_THUMBNAIL);

		String thumbnailImageUrl = fileStorage.upload(episodeRequest.getThumbnailImage(), ImageFileType.EPISODE_THUMBNAIL);

		callDeleteFileAfterRollbackEvent(thumbnailImageUrl, ImageFileType.EPISODE_THUMBNAIL);

		episode.updateEpisode(episodeRequest.getTitle(), thumbnailImageUrl);
	}

	private void deleteEpisodeContentImages(
			List<EpisodeImage> savedEpisodeImages,
			List<EpisodeContentRequest> episodeContentRequests
	) {
		List<EpisodeImage> removeEpisodeContentImages = getRemoveEpisodeContentImages(
				episodeContentRequests,
				savedEpisodeImages
		);

		episodeImageRepository.deleteAll(removeEpisodeContentImages);
		callDeleteEpisodeImagesToFileStorageEvent(removeEpisodeContentImages);
	}

	private void callDeleteEpisodeImagesToFileStorageEvent(List<EpisodeImage> removeEpisodeContentImages) {
		removeEpisodeContentImages.forEach(
				episodeImage -> callDeleteFileAfterCommitEvent(episodeImage.getContentImageUrl(), ImageFileType.EPISODE)
		);
	}

	private void createEpisodeContentImages(
			Episode episode,
			List<EpisodeImage> savedEpisodeImages,
			List<EpisodeContentRequest> episodeContentRequests
	) {
		List<EpisodeContentRequest> newEpisodeContentImages = getNewEpisodeContentImages(
				episodeContentRequests,
				savedEpisodeImages
		);

		newEpisodeContentImages.forEach(
				episodeContentRequest -> {
					String episodeImageUrl = fileStorage.upload(episodeContentRequest.getContentImage(), ImageFileType.EPISODE);
					EpisodeImage episodeImage = episodeContentRequest.toEpisodeImage(episodeImageUrl, episode);
					episodeImageRepository.save(episodeImage);

					callDeleteFileAfterRollbackEvent(episodeImageUrl, ImageFileType.EPISODE);
				}
		);
	}

	private void updateEpisodeContentImages(
			List<EpisodeImage> savedEpisodeImages,
			List<EpisodeContentRequest> episodeContentRequests
	) {
		savedEpisodeImages.forEach(episodeImage ->
				findSameOrderEpisodeImageAndUpdate(episodeContentRequests, episodeImage)
		);
	}

	private void findSameOrderEpisodeImageAndUpdate(List<EpisodeContentRequest> episodeContentRequests, EpisodeImage episodeImage) {
		episodeContentRequests.stream()
				.filter(
						episodeContentRequest -> episodeImage.isSameContentOrder(episodeContentRequest.getContentOrder())
				)
				.findFirst()
				.ifPresent(episodeContentRequest -> {
							String episodeImageUrl = fileStorage.upload(episodeContentRequest.getContentImage(), ImageFileType.EPISODE);
							episodeImage.updateEpisodeImage(episodeImageUrl);

							callDeleteFileAfterCommitEvent(episodeImage.getContentImageUrl(), ImageFileType.EPISODE);
							callDeleteFileAfterRollbackEvent(episodeImageUrl, ImageFileType.EPISODE);
						}
				);
	}

	private List<EpisodeContentRequest> getNewEpisodeContentImages(
			List<EpisodeContentRequest> episodeContentRequests,
			List<EpisodeImage> savedEpisodeImages
	) {
		List<Integer> savedEpisodeImageOrders = savedEpisodeImages.stream()
				.map(EpisodeImage::getContentOrder)
				.toList();

		return episodeContentRequests.stream()
				.filter(episodeContentRequest -> !savedEpisodeImageOrders.contains(episodeContentRequest.getContentOrder()))
				.toList();
	}

	private List<EpisodeImage> getRemoveEpisodeContentImages(
			List<EpisodeContentRequest> episodeContentRequests,
			List<EpisodeImage> savedEpisodeImages
	) {
		List<Integer> episodeContentImageOrders = episodeContentRequests.stream()
				.map(EpisodeContentRequest::getContentOrder)
				.toList();

		return savedEpisodeImages.stream()
				.filter(episodeImage -> !episodeContentImageOrders.contains(episodeImage.getContentOrder()))
				.toList();
	}

	private void callDeleteFileAfterCommitEvent(String fileUrl, FileType fileType) {
		applicationEventPublisher.publishEvent(
				new FileDeleteAfterCommitEvent(fileUrl, fileType)
		);
	}

	private void callDeleteFileAfterRollbackEvent(String fileUrl, FileType fileType) {
		applicationEventPublisher.publishEvent(
				new FileDeleteAfterCommitEvent(fileUrl, fileType)
		);
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

	private User getUser(LoginId loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private Comic getComicWithAuthor(Long comicId) {
		return comicRepository.findComicWithAuthor(comicId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMIC_NOT_FOUND));
	}

	private Episode getEpisodeWithComicAndAuthor(Long episodeId) {
		return episodeRepository.findByIdWithComicAndAuthor(episodeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.EPISODE_NOT_FOUND));
	}
}