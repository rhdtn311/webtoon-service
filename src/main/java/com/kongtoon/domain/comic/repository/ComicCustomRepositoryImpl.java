package com.kongtoon.domain.comic.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.kongtoon.domain.author.model.QAuthor;
import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.QComic;
import com.kongtoon.domain.comic.model.QThumbnail;
import com.kongtoon.domain.comic.model.ThumbnailType;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByViewRecentResponse;
import com.kongtoon.domain.episode.model.QEpisode;
import com.kongtoon.domain.view.model.QView;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
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

	QEpisode episode = new QEpisode("episode");
	QEpisode subEpisode = new QEpisode("sub_episode");

	@Override
	public List<ComicByGenreResponse> findComicsByGenre(Genre genre) {
		NumberExpression<Integer> orderByComicCreatedAt = getOrderByComicCreatedAt();

		return jpaQueryFactory.select(
						Projections.constructor(
								ComicByGenreResponse.class,
								comic.id, comic.name, comic.author.authorName, thumbnail.imageUrl, isNewComic()
						)
				)
				.from(comic)
				.leftJoin(episode)
				.on(episode.comic.eq(comic),
						episode.episodeNumber.eq(
								findLastEpisodes()
						)
				)
				.leftJoin(view)
				.on(view.episode.eq(episode))
				.leftJoin(thumbnail)
				.on(thumbnail.comic.eq(comic))
				.where(
						isSameGenre(genre)
								.and(
										isSameThumbnailType(ThumbnailType.MAIN)
								)
				)
				.groupBy(comic.id, thumbnail.imageUrl)
				.orderBy(orderByComicCreatedAt.asc(), view.id.count().desc())
				.fetch();
	}

	@Override
	public List<ComicByViewRecentResponse> findComicsByViewRecent(Long userId) {
		return jpaQueryFactory.select(
						Projections.constructor(
								ComicByViewRecentResponse.class,
								comic.id, comic.name, comic.author.authorName, thumbnail.imageUrl
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

	private JPQLQuery<Integer> findLastEpisodes() {

		return JPAExpressions.select(subEpisode.episodeNumber.max())
				.from(subEpisode)
				.where(subEpisode.comic.eq(comic));
	}
}

