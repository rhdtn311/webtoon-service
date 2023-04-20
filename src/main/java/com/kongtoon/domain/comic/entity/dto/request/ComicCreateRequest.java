package com.kongtoon.domain.comic.entity.dto.request;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.comic.entity.Genre;
import com.kongtoon.domain.comic.entity.PublishDayOfWeek;
import com.kongtoon.domain.comic.entity.Thumbnail;
import com.kongtoon.domain.comic.entity.ThumbnailType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ComicCreateRequest {
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

	private List<ThumbnailCreateRequest> thumbnailCreateRequests;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class ThumbnailCreateRequest {
		private ThumbnailType thumbnailType;
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
