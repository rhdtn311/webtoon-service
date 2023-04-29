package com.kongtoon.domain.follow.model.dto.response;

public record FollowResponse(
		int followCount,
		boolean isFollow
) {
	public static FollowResponse from(int followCount, boolean isFollow) {
		return new FollowResponse(followCount, isFollow);
	}
}
