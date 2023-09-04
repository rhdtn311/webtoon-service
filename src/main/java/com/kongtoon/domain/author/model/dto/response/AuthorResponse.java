package com.kongtoon.domain.author.model.dto.response;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.model.Comic;

import java.util.List;
import java.util.Map;

public record AuthorResponse(
		String authorName,
		String introduction,
		String belong,
		List<ComicResponse> comics
) {

	private static final int NOT_EXIST_EPISODE_NUMBER = 0;

	public static AuthorResponse from(
			Author author,
			List<Comic> comics,
			Map<Long, String> smallThumbnailUrlsOfComic,
			Map<Long, Integer> lastEpisodeNumbersOfComic
	) {
		return new AuthorResponse(
				author.getAuthorName(),
				author.getIntroduction(),
				author.getBelong(),
				ComicResponse.from(comics, smallThumbnailUrlsOfComic, lastEpisodeNumbersOfComic)
		);
	}

	record ComicResponse(
			String comicTitle,
			boolean isCompleted,
			String thumbnailUrl,
			int lastEpisodeNumber
	) {
		public static List<ComicResponse> from(
				List<Comic> comics,
				Map<Long, String> smallThumbnailUrlsOfComic,
				Map<Long, Integer> lastEpisodeNumberOfComic
		) {
			return comics.stream()
					.map(comic ->
							new ComicResponse(
									comic.getName(),
									comic.isComplete(),
									smallThumbnailUrlsOfComic.get(comic.getId()),
									lastEpisodeNumberOfComic.getOrDefault(comic.getId(), NOT_EXIST_EPISODE_NUMBER)
							)
					).toList();
		}
	}
}
