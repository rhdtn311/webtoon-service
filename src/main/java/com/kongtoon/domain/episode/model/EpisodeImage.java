package com.kongtoon.domain.episode.model;

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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "episode_image")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE episode_image SET deleted_at = NOW() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeImage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "content_image_url", length = 2048, nullable = false)
	private String contentImageUrl;

	@Column(name = "content_order", nullable = false)
	private int contentOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "episode_id", nullable = false)
	private Episode episode;

	public EpisodeImage(String contentImageUrl, int contentOrder, Episode episode) {
		this.contentImageUrl = contentImageUrl;
		this.contentOrder = contentOrder;
		this.episode = episode;
	}

	public boolean isSameContentOrder(int contentOrder) {
		return this.contentOrder == contentOrder;
	}

	public void updateEpisodeImage(String contentImageUrl) {
		this.contentImageUrl = contentImageUrl;
	}
}
