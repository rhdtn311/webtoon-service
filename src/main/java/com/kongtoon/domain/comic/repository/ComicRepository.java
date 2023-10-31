package com.kongtoon.domain.comic.repository;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.comic.model.Comic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComicRepository extends JpaRepository<Comic, Long>, ComicCustomRepository {

	@Query("SELECT DISTINCT c FROM Comic c JOIN FETCH c.thumbnails WHERE c.author = :author")
	List<Comic> findByAuthorWithThumbnails(@Param(value = "author") Author author);

	@Query("SELECT c FROM Comic c JOIN FETCH c.author WHERE c.id = :comicId")
	Optional<Comic> findComicWithAuthor(Long comicId);
}
