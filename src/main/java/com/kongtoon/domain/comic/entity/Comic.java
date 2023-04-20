package com.kongtoon.domain.comic.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.kongtoon.domain.BaseEntity;
import com.kongtoon.domain.author.model.Author;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comic")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE comic SET deleted_at = NOW() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comic extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 30, nullable = false)
	private String name;

	@Column(name = "genre", length = 10, nullable = false)
	@Enumerated(EnumType.STRING)
	private Genre genre;

	@Column(name = "summary", length = 500, nullable = false)
	private String summary;

	@Column(name = "publish_day_of_week", length = 3, nullable = false)
	@Enumerated(EnumType.STRING)
	private PublishDayOfWeek publishDayOfWeek;

	@Column(name = "follower_count", nullable = false)
	private int followerCount;

	@Column(name = "isComplete", nullable = false)
	private boolean isComplete;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private Author author;

	public Comic(String name, Genre genre, String summary, PublishDayOfWeek publishDayOfWeek, Author author) {
		this.isComplete = false;
		this.followerCount = 0;
		this.name = name;
		this.genre = genre;
		this.summary = summary;
		this.publishDayOfWeek = publishDayOfWeek;
		this.author = author;
	}
}
