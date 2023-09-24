package com.kongtoon.domain.comic.service;

import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByRealtimeRankingResponse;
import com.kongtoon.domain.comic.repository.ComicRepository;
import com.kongtoon.support.dummy.ComicDummy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComicReadServiceTest {

    @InjectMocks
    ComicReadService comicReadService;

    @Mock
    ComicRepository comicRepository;

    @Nested
    @Transactional
    @DisplayName("장르별 웹툰 목록 조회 성공")
    class GetComicsByGenreSuccess {

        @Test
        @DisplayName("장르별 웹툰 목록 조회에 성공한다.")
        void getComicsByGenreSuccess() {
            // given
            List<ComicByGenreResponse> expectedComicsByGenre = createActionGenreComics();

            // when
            List<ComicByGenreResponse> comicsByAnyGenreResult = getComicsByActionGenre(expectedComicsByGenre);

            // then
            assertThat(comicsByAnyGenreResult).containsAll(expectedComicsByGenre);
        }

        public List<ComicByGenreResponse> createActionGenreComics() {
            return List.of(
                    ComicDummy.createComicResponse(1L),
                    ComicDummy.createComicResponse(2L),
                    ComicDummy.createComicResponse(3L)
            );
        }

        private List<ComicByGenreResponse> getComicsByActionGenre(List<ComicByGenreResponse> comicByGenreResponses) {
            when(comicRepository.findComicsByGenre(Genre.ACTION))
                    .thenReturn(comicByGenreResponses);

            return comicReadService.getComicsByGenre(Genre.ACTION);
        }
    }

    @Nested
    @Transactional
    @DisplayName("실시간 인기 웹툰 목록 조회 성공")
    class GetRealtimeRankingComics {

        @Test
        @DisplayName("실시간 인기 웹툰 목록 조회에 성공한다.")
        void getComicsByRealtimeRankingSuccess() {
            // given
            List<ComicByRealtimeRankingResponse> expectedComicRankings = getComicByRealtimeRankingResponses();

            // when
            List<ComicByRealtimeRankingResponse> comicRankingsResult = comicReadService.getComicsByRealtimeRanking();

            // then
            assertThat(comicRankingsResult).containsAll(expectedComicRankings);
        }

        private List<ComicByRealtimeRankingResponse> getComicByRealtimeRankingResponses() {
            List<ComicByRealtimeRankingResponse> expectedComicRankings = ComicDummy.getComicByRealtimeRankingResponses();

            when(comicRepository.findComicsByRealtimeRanking(any(), any()))
                    .thenReturn(expectedComicRankings);

            return expectedComicRankings;
        }
    }
}