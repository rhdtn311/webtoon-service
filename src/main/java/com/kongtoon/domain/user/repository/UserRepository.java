package com.kongtoon.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByLoginId(String loginId);

	boolean existsByLoginId(String loginId);

	boolean existsByEmail(String email);
}