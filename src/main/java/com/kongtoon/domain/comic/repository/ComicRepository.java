package com.kongtoon.domain.comic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.entity.Comic;

public interface ComicRepository extends JpaRepository<Comic, Long> {

	List<Comic> findByAuthor(Author author);
}
