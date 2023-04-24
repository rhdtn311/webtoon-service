package com.kongtoon.domain.episode.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import com.kongtoon.domain.comic.entity.Comic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "episode")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE episode SET deleted_at = NOW() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Episode extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", length = 255, nullable = false)
	private String title;

	@Column(name = "episode_number", nullable = false)
	private int episodeNumber;

	@Column(name = "thumbnail_url", length = 2048, nullable = false)
	private String thumbnailUrl;

	@Column(name = "like_count", nullable = false)
	private int likeCount;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comic_id", nullable = false)
	private Comic comic;

	public Episode(String title, int episodeNumber, String thumbnailUrl, int likeCount,
			Comic comic) {
		this.title = title;
		this.episodeNumber = episodeNumber;
		this.thumbnailUrl = thumbnailUrl;
		this.likeCount = likeCount;
		this.comic = comic;
	}
}
