package com.kongtoon.domain.comic.repository;

import java.util.List;

import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByNewResponse;
import com.kongtoon.domain.comic.model.dto.response.ComicByViewRecentResponse;

public interface ComicCustomRepository {

	List<ComicByGenreResponse> findComicsByGenre(Genre genre);

	List<ComicByViewRecentResponse> findComicsByViewRecent(Long userId);

	List<ComicByNewResponse> findComicsByNew();
}
