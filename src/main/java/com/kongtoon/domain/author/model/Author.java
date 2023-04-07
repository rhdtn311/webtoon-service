package com.kongtoon.domain.author.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.kongtoon.domain.BaseEntity;
import com.kongtoon.domain.user.model.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "author")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE author SET deleted_at = NOW() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Author extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "author_name", length = 20, nullable = false)
	private String authorName;

	@Column(name = "introduction", length = 500, nullable = false)
	private String introduction;

	@Column(name = "belong", length = 15, nullable = true)
	private String belong;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public Author(String authorName, String introduction, String belong, User user) {
		this.authorName = authorName;
		this.introduction = introduction;
		this.belong = belong;
		this.user = user;
	}
}
