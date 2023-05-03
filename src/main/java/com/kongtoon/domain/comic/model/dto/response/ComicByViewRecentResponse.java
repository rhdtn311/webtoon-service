package com.kongtoon.domain.comic.model.dto.response;

public record ComicByViewRecentResponse(
		Long id,
		String name,
		String author,
		String thumbnailUrl
) {
}
