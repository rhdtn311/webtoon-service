package com.kongtoon.domain.user.repository;

import com.kongtoon.domain.user.model.LoginId;
import com.kongtoon.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByLoginId(LoginId loginId);

	boolean existsByLoginId(LoginId loginId);

	boolean existsByEmail(String email);
}