package com.kongtoon.domain.comic.model;

import java.time.LocalDate;

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

import com.kongtoon.domain.BaseEntity;
import com.kongtoon.domain.comic.model.dto.response.vo.TwoHourSlice;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "realtime_comic_ranking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealtimeComicRanking extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "record_date", nullable = false)
	private LocalDate recordDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "record_time", nullable = false)
	private TwoHourSlice recordTime;

	@Column(name = "ranks", nullable = false)
	private int rank;

	@Column(name = "views", nullable = false)
	private long views;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comic_id", nullable = false)
	private Comic comic;

	public RealtimeComicRanking(LocalDate recordDate, TwoHourSlice recordTime, int rank, long views, Comic comic) {
		this.recordDate = recordDate;
		this.recordTime = recordTime;
		this.rank = rank;
		this.views = views;
		this.comic = comic;
	}
}
