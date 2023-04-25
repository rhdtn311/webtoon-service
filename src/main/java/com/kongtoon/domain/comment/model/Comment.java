package com.kongtoon.domain.comment.model;

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
import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.model.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE comment SET deleted_at = NOW() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "content", length = 500, nullable = false)
	private String content;

	@Column(name = "parent_id", nullable = true)
	private Long parentId;

	@Column(name = "like_count", nullable = false)
	private int likeCount;

	@Column(name = "visible", nullable = false)
	private boolean visible;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "episode_id")
	private Episode episode;

	public Comment(String content, Long parentId, User user, Episode episode) {
		this.likeCount = 0;
		this.visible = true;
		this.content = content;
		this.parentId = parentId;
		this.user = user;
		this.episode = episode;
	}
}
