package com.kongtoon.domain.episode.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.episode.model.EpisodeImage;

public interface EpisodeImageRepository extends JpaRepository<EpisodeImage, Long> {
}
