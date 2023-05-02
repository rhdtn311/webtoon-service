package com.kongtoon.domain.comic.model.dto.response;

public record ComicByGenreResponse(
		Long id,
		String name,
		String author,
		String thumbnailUrl,
		boolean isNew
) {
}
