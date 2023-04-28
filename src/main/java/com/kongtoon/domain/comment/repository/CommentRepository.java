package com.kongtoon.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.comment.model.Comment;
import com.kongtoon.domain.episode.model.Episode;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	int countByEpisode(Episode episode);
}
