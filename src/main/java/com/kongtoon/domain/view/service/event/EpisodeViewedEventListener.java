package com.kongtoon.domain.view.service.event;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.view.model.View;
import com.kongtoon.domain.view.repository.ViewRepository;
import com.kongtoon.domain.view.repository.cache.ViewCache;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EpisodeViewedEventListener {

	private final ViewCache viewCache;
	private final ViewRepository viewRepository;

	@Async
	@EventListener
	public synchronized void saveView(EpisodeViewedEvent episodeViewedEvent) {
		User user = episodeViewedEvent.getUser();
		Episode episode = episodeViewedEvent.getEpisode();

		try {
			viewCache.save(user, episode);
		} catch (Exception e) {
			saveViewInDB(user, episode);
		}

		checkAndBulkInsert();
		checkAndBulkUpdate();
	}

	private void saveViewInDB(User user, Episode episode) {
		viewRepository.findByUserAndEpisode(user, episode)
				.ifPresentOrElse(viewRepository::save,
						() -> viewRepository.save(new View(user, episode))
				);
	}

	private void checkAndBulkUpdate() {
		if (viewCache.checkUpdate()) {
			viewCache.batchUpdateToDB();
		}
	}

	private void checkAndBulkInsert() {
		if (viewCache.checkInsert()) {
			viewCache.batchInsertToDB();
		}
	}
}
