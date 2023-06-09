package com.kongtoon.domain.comic.model.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import com.kongtoon.common.validation.NotEmptyFile;
import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.PublishDayOfWeek;
import com.kongtoon.domain.comic.model.Thumbnail;
import com.kongtoon.domain.comic.model.ThumbnailType;
import com.kongtoon.domain.comic.model.validation.ComicRequestValid;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ComicRequestValid
public class ComicRequest {

	@Length(min = 1, max = 30)
	@NotBlank
	private String comicName;

	@NotNull
	private Genre genre;

	@Length(min = 1, max = 500)
	@NotBlank
	private String summary;

	@NotNull
	private PublishDayOfWeek publishDayOfWeek;

	@Valid
	@NotNull
	private List<ThumbnailRequest> thumbnailRequests;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class ThumbnailRequest {

		@NotNull
		private ThumbnailType thumbnailType;

		@NotEmptyFile
		private MultipartFile thumbnailImage;

		public Thumbnail toThumbnail(String thumbnailImageUrl, Comic comic) {
			return new Thumbnail(
					this.thumbnailType,
					thumbnailImageUrl,
					comic
			);
		}
	}

	public Comic toComic(Author author) {
		return new Comic(
				this.comicName,
				this.genre,
				this.summary,
				this.publishDayOfWeek,
				author
		);
	}
}
