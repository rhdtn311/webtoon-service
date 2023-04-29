package com.kongtoon.domain.view.service.event;

import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.view.model.View;
import com.kongtoon.domain.view.repository.ViewRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EpisodeViewedEventListener {

	private final ViewRepository viewRepository;

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void saveView(EpisodeViewedEvent episodeViewedEvent) {
		User user = episodeViewedEvent.getUser();
		Episode episode = episodeViewedEvent.getEpisode();

		View view = getViewIfPresent(user, episode)
				.orElse(episodeViewedEvent.toView());

		view.updateLastAccessTime();

		viewRepository.save(view);
	}

	private Optional<View> getViewIfPresent(User user, Episode episode) {
		return viewRepository.findByUserAndEpisode(user, episode);
	}
}
