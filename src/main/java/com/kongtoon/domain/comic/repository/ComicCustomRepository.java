package com.kongtoon.domain.comic.repository;

import java.time.LocalDate;
import java.util.List;

import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.RealtimeComicRanking;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByNewResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByRealtimeRankingResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByViewRecentResponse;
import com.kongtoon.domain.comic.model.dto.response.vo.TwoHourSlice;

public interface ComicCustomRepository {

	List<ComicByGenreResponse> findComicsByGenre(Genre genre);

	List<ComicByViewRecentResponse> findComicsByViewRecent(Long userId);

	List<ComicByNewResponse> findComicsByNew();

	List<RealtimeComicRanking> findRealtimeComicRankingForSave(LocalDate recordDate, TwoHourSlice recordTime);

	List<ComicByRealtimeRankingResponse> findComicsByRealtimeRanking(LocalDate recordDate, TwoHourSlice recordTime);
}
