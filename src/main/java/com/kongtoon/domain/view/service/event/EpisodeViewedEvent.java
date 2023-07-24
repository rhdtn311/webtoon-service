package com.kongtoon.domain.view.service.event;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.view.model.View;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EpisodeViewedEvent {

	private final User user;
	private final Episode episode;

	public View toView() {
		return new View(
				user,
				episode
		);
	}
}
