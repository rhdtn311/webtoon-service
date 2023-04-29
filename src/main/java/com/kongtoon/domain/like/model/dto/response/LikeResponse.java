package com.kongtoon.domain.like.model.dto.response;

public record LikeResponse(
		int likeCount,
		boolean isLike
) {
	public static LikeResponse from(int likeCount, boolean isLike) {
		return new LikeResponse(likeCount, isLike);
	}
}
