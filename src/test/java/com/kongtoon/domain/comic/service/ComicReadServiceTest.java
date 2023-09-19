package com.kongtoon.domain.comic.service;

import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.comic.repository.ComicRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.kongtoon.utils.TestUtil.createActionGenreComics;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComicReadServiceTest {

    @InjectMocks
    ComicReadService comicReadService;

    @Mock
    ComicRepository comicRepository;

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

    private List<ComicByGenreResponse> getComicsByActionGenre(List<ComicByGenreResponse> comicByGenreResponses) {
        when(comicRepository.findComicsByGenre(Genre.ACTION))
                .thenReturn(comicByGenreResponses);

        return comicReadService.getComicsByGenre(Genre.ACTION);
    }
}