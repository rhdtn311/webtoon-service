package com.kongtoon.domain.star.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.star.model.Star;
import com.kongtoon.domain.user.model.User;

public interface StarRepository extends JpaRepository<Star, Long> {

	boolean existsByUserAndEpisode(User user, Episode episode);

	int countByEpisode(Episode episode);

	@Query("SELECT avg(s.score) FROM Star s WHERE s.episode = :episode")
	Optional<Double> findAvgScoreByEpisode(Episode episode);
}