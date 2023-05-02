package com.kongtoon.domain.comic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kongtoon.domain.comic.model.Genre;
import com.kongtoon.domain.comic.model.dto.response.ComicByGenreResponse;
import com.kongtoon.domain.comic.repository.ComicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComicReadService {

	private final ComicRepository comicRepository;

	@Transactional(readOnly = true)
	public List<ComicByGenreResponse> getComicsByGenre(Genre genre) {

		return comicRepository.findComicsByGenre(genre);
	}
}
