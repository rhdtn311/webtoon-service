package com.kongtoon.domain.author.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kongtoon.domain.author.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}