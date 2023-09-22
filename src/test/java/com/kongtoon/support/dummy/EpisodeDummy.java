package com.kongtoon.support.dummy;

import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.episode.model.Episode;

public class EpisodeDummy {
    public static Episode createEpisode(Comic comic) {
        return new Episode("title", 1, "thumbnailUrl", comic);
    }
}
