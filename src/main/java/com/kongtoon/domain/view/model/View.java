package com.kongtoon.domain.view.model;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.model.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "view")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class View {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "first_access_time", nullable = false)
	private LocalDateTime firstAccessTime;

	@Column(name = "last_access_time", nullable = false)
	private LocalDateTime lastAccessTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "episode_id", nullable = false)
	private Episode episode;

	public View(User user, Episode episode) {
		this.user = user;
		this.episode = episode;
		this.firstAccessTime = LocalDateTime.now();
		this.lastAccessTime = LocalDateTime.now();
	}

	public void updateLastAccessTime() {
		this.lastAccessTime = LocalDateTime.now();
	}
}
