package com.kongtoon.domain.comic.model.dto.response;

public record ComicByRealtimeRankingResponse(
		Long id,
		int rank,
		String name,
		String author,
		String thumbnailUrl,
		Long viewCount
) {
	public static ComicByRealtimeRankingResponse from(
			Long id,
			int rank,
			String name,
			String author,
			String thumbnailUrl,
			Long viewCount
	) {
		return new ComicByRealtimeRankingResponse(id, rank, name, author, thumbnailUrl, viewCount);
	}
}
