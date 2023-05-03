package com.kongtoon.domain.comic.model.dto.response;

public record ComicByNewResponse(
		Long id,
		String name,
		String author,
		String thumbnailUrl,
		Long newEpisodeId
) {
}
