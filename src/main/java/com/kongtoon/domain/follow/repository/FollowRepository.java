package com.kongtoon.domain.follow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.comic.entity.Comic;
import com.kongtoon.domain.follow.model.Follow;
import com.kongtoon.domain.user.model.User;

public interface FollowRepository extends JpaRepository<Follow, Long> {

	boolean existsByUserAndComic(User user, Comic comic);

	int countByComic(Comic comic);

	Optional<Follow> findByUserAndComic(User user, Comic comic);
}
