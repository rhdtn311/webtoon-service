package com.kongtoon.domain.episode.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.episode.model.Episode;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

	@Query(
			"SELECT e "
					+ "FROM Episode e "
					+ "WHERE e.episodeNumber = "
					+ "(SELECT MAX(sub_e.episodeNumber) FROM Episode sub_e WHERE sub_e.comic = e.comic)"
					+ "AND e.comic IN (:comics)"
	)
	List<Episode> findRecentlyEpisodesByComics(List<Comic> comics);

	@Query(
			"SELECT e "
					+ "FROM Episode e "
					+ "JOIN FETCH e.comic c "
					+ "JOIN FETCH c.author a "
					+ "WHERE e.id = :episodeId"
	)
	Optional<Episode> findByIdWithComicAndAuthor(Long episodeId);

	@Query(
			"SELECT e "
					+ "FROM Episode e "
					+ "JOIN FETCH e.comic c "
					+ "JOIN FETCH c.author "
					+ "WHERE c.id = :comicId"
	)
	List<Episode> findByComicIdWithComicAndAuthor(Long comicId);

	@Query(
			"SELECT e "
					+ "FROM Episode e "
					+ "JOIN FETCH e.comic "
					+ "WHERE e.id = :episodeId"
	)
	Optional<Episode> findByIdWithComic(Long episodeId);

	Optional<Episode> findFirstByComicOrderByEpisodeNumberDesc(Comic comic);
}
