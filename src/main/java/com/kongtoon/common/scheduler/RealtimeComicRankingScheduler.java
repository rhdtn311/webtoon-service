package com.kongtoon.common.scheduler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kongtoon.domain.comic.model.RealtimeComicRanking;
import com.kongtoon.domain.comic.model.dto.response.vo.TwoHourSlice;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.domain.comic.repository.RealtimeComicRankingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RealtimeComicRankingScheduler {

	private final ComicRepository comicRepository;
	private final RealtimeComicRankingRepository realtimeComicRankingRepository;

	@Transactional
	@Scheduled(cron = "0 59 1/2 * * *")
	public void test() {
		log.info("실시간 인기 웹툰 목록 조회 스케줄링 시작");

		LocalDate recordDate = LocalDate.now();
		TwoHourSlice recordTime = TwoHourSlice.getNow(LocalTime.now());

		List<RealtimeComicRanking> realtimeComicRankings = comicRepository.findRealtimeComicRankingForSave(
				recordDate,
				recordTime
		);

		realtimeComicRankingRepository.saveAll(realtimeComicRankings);

		log.info("실시간 인기 웹툰 목록 조회 스케줄링 종료");
	}
}
