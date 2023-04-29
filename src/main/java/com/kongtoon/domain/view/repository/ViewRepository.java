package com.kongtoon.domain.view.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.view.model.View;

public interface ViewRepository extends JpaRepository<View, Long> {

	List<View> findByUserAndEpisodeIn(User user, List<Episode> episodes);

	Optional<View> findByUserAndEpisode(User user, Episode episode);
}