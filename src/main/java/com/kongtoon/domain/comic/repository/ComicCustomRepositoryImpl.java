package com.kongtoon.domain.comic.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.kongtoon.domain.author.model.QAuthor;
import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.QComic;
import com.kongtoon.domain.comic.model.QRealtimeComicRanking;
import com.kongtoon.domain.comic.model.QThumbnail;
import com.kongtoon.domain.comic.model.RealtimeComicRanking;
import com.kongtoon.domain.comic.model.ThumbnailType;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByNewResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByRealtimeRankingResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByViewRecentResponse;
import com.kongtoon.domain.comic.model.dto.response.vo.TwoHourSlice;
import com.kongtoon.domain.episode.model.QEpisode;
import com.kongtoon.domain.view.model.QView;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ComicCustomRepositoryImpl implements ComicCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	QComic comic = QComic.comic;
	QAuthor author = QAuthor.author;
	QView view = QView.view;
	QThumbnail thumbnail = QThumbnail.thumbnail;
	QRealtimeComicRanking realtimeComicRanking = QRealtimeComicRanking.realtimeComicRanking;

	QEpisode episode = new QEpisode("episode");
	QEpisode subEpisode = new QEpisode("sub_episode");

	@Override
	public List<ComicByGenreResponse> findComicsByGenre(Genre genre) {
		NumberExpression<Integer> orderByComicCreatedAt = getOrderByComicCreatedAt();

		return jpaQueryFactory.select(
						Projections.constructor(
								ComicByGenreResponse.class,
								comic.id, comic.name, author.authorName, thumbnail.imageUrl, isNewComic(), view.id.count()
						)
				)
				.from(comic)
				.leftJoin(episode)
				.on(
						episode.comic.eq(comic),
						Expressions.list(episode.comic.id, episode.episodeNumber).in(findLastEpisodesGroupByComic())
				)
				.leftJoin(view)
				.on(view.episode.eq(episode))
				.leftJoin(thumbnail)
				.on(comic.eq(thumbnail.comic), isSameThumbnailType(ThumbnailType.MAIN))
				.join(author)
				.on(author.eq(comic.author))
				.where(isSameGenre(genre))
				.groupBy(comic.id, thumbnail.imageUrl)
				.orderBy(orderByComicCreatedAt.asc(), view.id.count().desc())
				.fetch();
	}

	@Override
	public List<ComicByViewRecentResponse> findComicsByViewRecent(Long userId) {
		return jpaQueryFactory.select(
						Projections.constructor(
								ComicByViewRecentResponse.class,
								comic.id, comic.name, author.authorName, thumbnail.imageUrl
						)
				)
				.from(view)
				.join(view.episode, episode)
				.join(episode.comic, comic)
				.join(comic.author, author)
				.join(thumbnail).on(comic.eq(thumbnail.comic), isSameThumbnailType(ThumbnailType.SMALL))
				.where(isSameUser(userId))
				.groupBy(comic.id, thumbnail.imageUrl)
				.orderBy(view.lastAccessTime.max().desc())
				.limit(10)
				.fetch();
	}

	@Override
	public List<ComicByNewResponse> findComicsByNew() {

		return jpaQueryFactory.select(
						Projections.constructor(
								ComicByNewResponse.class,
								comic.id, comic.name, author.authorName, thumbnail.imageUrl, episode.id
						)
				).from(comic)
				.join(episode).on(comic.eq(episode.comic), episode.episodeNumber.eq(findLastEpisodes()))
				.join(comic.author, author)
				.join(thumbnail).on(comic.eq(thumbnail.comic), isSameThumbnailType(ThumbnailType.SMALL))
				.where(isNewEpisode())
				.orderBy(episode.createdAt.desc())
				.fetch();
	}

	@Override
	public List<RealtimeComicRanking> findRealtimeComicRankingForSave(LocalDate recordDate, TwoHourSlice recordTime) {

		List<Tuple> resultSet = jpaQueryFactory.select(comic, view.id.count())
				.from(view)
				.leftJoin(view.episode, episode)
				.join(episode.comic, comic)
				.where(isBetweenTime(recordDate, recordTime))
				.groupBy(comic.id)
				.orderBy(view.id.count().desc())
				.limit(10)
				.fetch();

		return toRealtimeComicRankings(resultSet, recordDate, recordTime);
	}

	@Override
	public List<ComicByRealtimeRankingResponse> findComicsByRealtimeRanking(
			LocalDate recordDate,
			TwoHourSlice recordTime
	) {
		return jpaQueryFactory.select(
						Projections.constructor(
								ComicByRealtimeRankingResponse.class,
								comic.id, realtimeComicRanking.rank, comic.name, author.authorName, thumbnail.imageUrl,
								realtimeComicRanking.views)
				)
				.from(realtimeComicRanking)
				.join(realtimeComicRanking.comic, comic)
				.join(comic.author, author)
				.leftJoin(thumbnail).on(comic.eq(thumbnail.comic), isSameThumbnailType(ThumbnailType.SMALL))
				.where(isSameRecordDateAndRecordTime(recordDate, recordTime))
				.orderBy(realtimeComicRanking.rank.asc())
				.fetch();
	}

	private BooleanExpression isSameRecordDateAndRecordTime(LocalDate recordDate, TwoHourSlice recordTime) {
		return realtimeComicRanking.recordDate.eq(recordDate)
				.and(realtimeComicRanking.recordTime.eq(recordTime));
	}

	private List<RealtimeComicRanking> toRealtimeComicRankings(
			List<Tuple> resultSet,
			LocalDate recordDate,
			TwoHourSlice recordTime
	) {
		int rank = 1;
		List<RealtimeComicRanking> realtimeComicRankings = new ArrayList<>();

		for (Tuple tuple : resultSet) {
			Comic foundComic = tuple.get(comic);
			Long views = tuple.get(view.id.count());

			realtimeComicRankings.add(
					new RealtimeComicRanking(recordDate, recordTime, rank++, views, foundComic));
		}

		return realtimeComicRankings;
	}

	private BooleanExpression isBetweenTime(LocalDate recordDate, TwoHourSlice recordTime) {

		return view.lastAccessTime.between(
				recordTime.getStartTime().atDate(recordDate),
				recordTime.getEndTime().atDate(recordDate)
		);
	}

	private BooleanExpression isSameGenre(Genre genre) {
		return comic.genre.eq(genre);
	}

	private BooleanExpression isSameThumbnailType(ThumbnailType thumbnailType) {
		return thumbnail.thumbnailType.eq(thumbnailType);
	}

	private BooleanExpression isSameUser(Long userId) {
		return view.user.id.eq(userId);
	}

	private NumberExpression<Integer> getOrderByComicCreatedAt() {

		return new CaseBuilder()
				.when(isNewComic()).then(1)
				.otherwise(2);
	}

	private BooleanExpression isNewComic() {
		LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

		return comic.createdAt.after(threeDaysAgo);
	}

	private BooleanExpression isNewEpisode() {
		LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);

		return episode.createdAt.after(twoDaysAgo);
	}

	private JPQLQuery<Integer> findLastEpisodes() {

		return JPAExpressions.select(subEpisode.episodeNumber.max())
				.from(subEpisode)
				.where(subEpisode.comic.eq(comic));
	}

	private JPQLQuery<Tuple> findLastEpisodesGroupByComic() {
		return JPAExpressions.select(subEpisode.comic.id, subEpisode.episodeNumber.max())
				.from(subEpisode)
				.groupBy(subEpisode.comic.id);
	}
}

