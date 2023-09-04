package com.kongtoon.domain.comic.model;

import com.kongtoon.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

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

	public Thumbnail(ThumbnailType thumbnailType, String imageUrl, Comic comic) {
		this.thumbnailType = thumbnailType;
		this.imageUrl = imageUrl;

		if (this.comic != null) {
			this.comic.getThumbnails().remove(this);
		}
		this.comic = comic;
		comic.getThumbnails().add(this);
	}

	public void update(ThumbnailType thumbnailType, String imageUrl) {
		this.thumbnailType = thumbnailType;
		this.imageUrl = imageUrl;
	}
}
