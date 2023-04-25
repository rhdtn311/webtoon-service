package com.kongtoon.domain.episode.model.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import com.kongtoon.common.validation.NotEmptyFile;
import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.model.EpisodeImage;
import com.kongtoon.domain.episode.model.validation.EpisodeRequestValid;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EpisodeRequestValid
@Getter
@Setter
@NoArgsConstructor
public class EpisodeRequest {

	@Length(min = 1, max = 255)
	@NotBlank
	private String title;

	@NotEmptyFile
	private MultipartFile thumbnailImage;

	@Valid
	private List<EpisodeContentRequest> episodeContentRequests;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class EpisodeContentRequest {

		@NotNull
		@NotEmptyFile
		private MultipartFile contentImage;

		@Min(value = 0)
		private int contentOrder;

		public EpisodeImage toEpisodeImage(String contentImageUrl, Episode episode) {
			return new EpisodeImage(
					contentImageUrl,
					this.contentOrder,
					episode
			);
		}
	}

	public Episode toEpisode(int episodeNumber, String thumbnailImageUrl, Comic comic) {
		return new Episode(
				this.title,
				episodeNumber,
				thumbnailImageUrl,
				0,
				comic
		);
	}
}


