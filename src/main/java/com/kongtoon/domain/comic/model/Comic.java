package com.kongtoon.domain.comic.model;

import com.kongtoon.domain.BaseEntity;
import com.kongtoon.domain.author.model.Author;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

	@Column(name = "is_complete", nullable = false)
	private boolean isComplete;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private Author author;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "comic")
	private List<Thumbnail> thumbnails = new ArrayList<>();

	public Comic(String name, Genre genre, String summary, PublishDayOfWeek publishDayOfWeek, Author author) {
		this.isComplete = false;
		this.name = name;
		this.genre = genre;
		this.summary = summary;
		this.publishDayOfWeek = publishDayOfWeek;
		this.author = author;
	}

	public void update(String name, Genre genre, String summary, PublishDayOfWeek publishDayOfWeek) {
		this.name = name;
		this.genre = genre;
		this.summary = summary;
		this.publishDayOfWeek = publishDayOfWeek;
	}
}
