package com.kongtoon.domain.episode.model.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.comic.entity.PublishDayOfWeek;
import com.kongtoon.domain.episode.model.Episode;

public record EpisodeListResponses(
		ComicInfo comicInfo,
		List<EpisodeListResponse> episodes
) {

	public record EpisodeListResponse(
			Long id,
			int episodeNumber,
			String title,
			@JsonFormat(pattern = "yyyy.MM.dd")
			LocalDate serviceDate,
			String thumbnailUrl,
			boolean isRead
	) {
		public static EpisodeListResponse from(Episode episode, boolean isRead) {
			return new EpisodeListResponse(
					episode.getId(),
					episode.getEpisodeNumber(),
					episode.getTitle(),
					episode.getCreatedAt().toLocalDate(),
					episode.getThumbnailUrl(),
					isRead
			);
		}
	}

	public record ComicInfo(
			Long id,
			String comicMainImageUrl,
			String comicName,
			String summary,
			String authorName,
			PublishDayOfWeek publishDayOfWeek
	) {

		public static ComicInfo from(Comic comic, String comicMainThumbnailImageUrl) {
			return new ComicInfo(
					comic.getId(),
					comicMainThumbnailImageUrl,
					comic.getName(),
					comic.getSummary(),
					comic.getAuthor().getAuthorName(),
					comic.getPublishDayOfWeek()
			);
		}
	}
}
