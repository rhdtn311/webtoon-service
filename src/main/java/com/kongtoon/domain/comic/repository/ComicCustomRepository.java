package com.kongtoon.domain.comic.repository;

import java.util.List;

import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;

public interface ComicCustomRepository {

	List<ComicByGenreResponse> findComicsByGenre(Genre genre);
}
