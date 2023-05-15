package com.kongtoon.domain.comic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.comic.model.RealtimeComicRanking;

public interface RealtimeComicRankingRepository extends JpaRepository<RealtimeComicRanking, Long> {
}
