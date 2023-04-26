package com.kongtoon.domain.episode.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.episode.model.EpisodeImage;

public interface EpisodeImageRepository extends JpaRepository<EpisodeImage, Long> {

	List<EpisodeImage> findByEpisode(Episode episode);
}
