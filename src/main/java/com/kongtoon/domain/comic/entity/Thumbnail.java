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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "thumbnail")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE thumbnail SET deleted_at = NOW() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Thumbnail extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "thumbnail_type", length = 10, nullable = false)
	private ThumbnailType thumbnailType;

	@Column(name = "image_url", length = 2048, nullable = false)
	private String imageUrl;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comic_id", nullable = false)
	private Comic comic;

	public Thumbnail(ThumbnailType thumbnailType, String imageUrl, LocalDateTime deletedAt, Comic comic) {
		this.thumbnailType = thumbnailType;
		this.imageUrl = imageUrl;
		this.deletedAt = deletedAt;
		this.comic = comic;
	}
}
