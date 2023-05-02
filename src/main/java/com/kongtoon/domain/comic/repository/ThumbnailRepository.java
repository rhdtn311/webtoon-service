package com.kongtoon.domain.comic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.Thumbnail;
import com.kongtoon.domain.comic.model.ThumbnailType;

public interface ThumbnailRepository extends JpaRepository<Thumbnail, Long> {

	List<Thumbnail> findByComicInAndThumbnailType(List<Comic> comics, ThumbnailType thumbnailType);

	List<Thumbnail> findByComic(Comic comic);

	Optional<Thumbnail> findByComicAndThumbnailType(Comic comic, ThumbnailType thumbnailType);
}
