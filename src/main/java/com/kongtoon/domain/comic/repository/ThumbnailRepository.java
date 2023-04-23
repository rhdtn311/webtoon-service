package com.kongtoon.domain.comic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.comic.entity.Thumbnail;
import com.kongtoon.domain.comic.entity.ThumbnailType;

public interface ThumbnailRepository extends JpaRepository<Thumbnail, Long> {

	List<Thumbnail> findByComicInAndThumbnailType(List<Comic> comics, ThumbnailType thumbnailType);

	List<Thumbnail> findByComic(Comic comic);
}
