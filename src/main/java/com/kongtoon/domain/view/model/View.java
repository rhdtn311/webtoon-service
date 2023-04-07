package com.kongtoon.domain.view.model;

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

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.kongtoon.domain.episode.domain.Episode;
import com.kongtoon.domain.user.model.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class View {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Column(name = "first_access_time", nullable = false)
	private LocalDateTime firstAccessTime;

	@LastModifiedDate
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
	}
}
