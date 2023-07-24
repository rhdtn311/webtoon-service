package com.kongtoon.domain.view.repository.cache;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.view.model.View;

import java.util.List;

public interface ViewCache {

    String INSERT_MAP_MAIN_KEY = "insert";
    String UPDATE_MAP_MAIN_KEY = "update";
    String USER_LOGIN_ID_PREFIX = "userId:";
    String EPISODE_ID_PREFIX = "episodeId:";
    long LIMIT_SIZE = 5000L;

    default String createKey(String loginId, Long episodeId) {
        return USER_LOGIN_ID_PREFIX + loginId + EPISODE_ID_PREFIX + episodeId;
    }

    void save(User user, Episode episode);

    boolean existsKey(String mainKey, String subKey);

    List<View> getValues(String mainKey);

    void clear(String mainKey);

    boolean checkInsert();

    boolean checkUpdate();
}