package com.kongtoon.domain.author.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.user.model.User;

public interface AuthorRepository extends JpaRepository<Author, Long> {

	Optional<Author> findByUser(User user);
}