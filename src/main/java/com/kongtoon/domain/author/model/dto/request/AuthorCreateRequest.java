package com.kongtoon.domain.author.model.dto.request;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.lang.Nullable;

import com.kongtoon.domain.author.model.Author;
import com.kongtoon.domain.user.model.User;

public record AuthorCreateRequest(
		@NotBlank
		@Length(max = 20)
		String authorName,

		@NotBlank
		@Length(max = 15)
		String belong,

		@Nullable
		@Length(max = 500)
		String introduction
) {

	public Author toEntity(User user) {
		return new Author(authorName, introduction, belong, user);
	}
}