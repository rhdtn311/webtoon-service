package com.kongtoon.domain.episode.repository;

import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.episode.model.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

	@Query(
			"SELECT e FROM Episode e WHERE e.comic IN (:comics)"
	)
	List<Episode> findEpisodesByComics(@Param(value = "comics") List<Comic> comics);

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
