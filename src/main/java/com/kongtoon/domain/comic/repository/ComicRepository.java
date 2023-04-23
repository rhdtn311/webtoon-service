package com.kongtoon.domain.comic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.entity.Comic;

public interface ComicRepository extends JpaRepository<Comic, Long> {

	List<Comic> findByAuthor(Author author);

	@Query("SELECT c FROM Comic c JOIN FETCH c.author WHERE c.id = :comicId")
	Optional<Comic> findComicWithAuthor(Long comicId);
}
