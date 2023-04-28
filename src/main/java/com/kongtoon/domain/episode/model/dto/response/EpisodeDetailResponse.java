package com.kongtoon.domain.episode.model.dto.response;

public record EpisodeDetailResponse(
		int commentCount,
		LikeResponse likeInfo,
		FollowResponse followInfo,
		StarResponse starInfo
) {

	public static EpisodeDetailResponse from(
			int commentCount,
			LikeResponse likeResponse,
			FollowResponse followResponse,
			StarResponse starResponse
	) {
		return new EpisodeDetailResponse(
				commentCount,
				likeResponse,
				followResponse,
				starResponse
		);
	}

	public record LikeResponse(
			int likeCount,
			boolean isLike
	) {
		public static LikeResponse from(int likeCount, boolean isLike) {
			return new LikeResponse(likeCount, isLike);
		}
	}

	public record FollowResponse(
			int followerCount,
			boolean isFollow
	) {
		public static FollowResponse from(int followerCount, boolean isFollow) {
			return new FollowResponse(followerCount, isFollow);
		}
	}

	public record StarResponse(
			int starCount,
			double starScore,
			boolean isStar
	) {
		public static StarResponse from(int starCount, double starScore, boolean isStar) {
			return new StarResponse(starCount, starScore, isStar);
		}
	}
}
