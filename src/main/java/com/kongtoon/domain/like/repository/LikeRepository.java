package com.kongtoon.domain.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.like.model.Like;
import com.kongtoon.domain.like.model.LikeType;
import com.kongtoon.domain.user.model.User;

public interface LikeRepository extends JpaRepository<Like, Long> {

	boolean existsByUserAndLikeTypeAndReferenceId(User user, LikeType likeType, Long referenceId);

	int countByLikeTypeAndReferenceId(LikeType likeType, Long referenceId);
}