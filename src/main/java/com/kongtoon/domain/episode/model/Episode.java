package com.kongtoon.domain.episode.model;

import com.kongtoon.domain.BaseEntity;
import com.kongtoon.domain.comic.model.Comic;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

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

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comic_id", nullable = false)
	private Comic comic;

	public Episode(String title, int episodeNumber, String thumbnailUrl, Comic comic) {
		this.title = title;
		this.episodeNumber = episodeNumber;
		this.thumbnailUrl = thumbnailUrl;
		this.comic = comic;
	}

	public void updateEpisode(String title, String thumbnailUrl) {
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
	}

	public boolean isSame(Episode episode) {
		return this == episode;
	}

    public Long getComicId() {
        return comic.getId();
    }
}
