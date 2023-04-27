package com.kongtoon.domain.episode.model.dto.response;

import java.util.List;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.model.EpisodeImage;

public record EpisodeResponse(
		int episodeNumber,
		String episodeTitle,
		List<EpisodeImageResponse> contentImages
) {

	public static EpisodeResponse from(Episode episode, List<EpisodeImageResponse> contentImages) {
		return new EpisodeResponse(
				episode.getEpisodeNumber(),
				episode.getTitle(),
				contentImages
		);
	}

	public record EpisodeImageResponse(
			String episodeImages,
			int order
	) {

		private static EpisodeImageResponse toEpisodeImageResponses(EpisodeImage episodeImage) {
			return new EpisodeImageResponse(episodeImage.getContentImageUrl(), episodeImage.getContentOrder());
		}

		public static List<EpisodeImageResponse> toEpisodeImageResponses(List<EpisodeImage> episodeImages) {
			return episodeImages.stream()
					.map(EpisodeImageResponse::toEpisodeImageResponses)
					.toList();
		}
	}
}