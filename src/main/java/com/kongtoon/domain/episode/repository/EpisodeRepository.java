package com.kongtoon.domain.episode.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.episode.domain.Episode;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

	@Query(
			"SELECT e "
					+ "FROM Episode e "
					+ "WHERE e.episodeNumber = "
					+ "(SELECT MAX(sub_e.episodeNumber) FROM Episode sub_e WHERE sub_e.comic = e.comic)"
					+ "AND e.comic IN (:comics)"
	)
	List<Episode> findRecentlyEpisodesByComics(List<Comic> comics);
}
