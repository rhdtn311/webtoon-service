package com.kongtoon.domain.view.repository.cache;

import com.kongtoon.domain.episode.model.Episode;
import com.kongtoon.domain.user.model.User;
import com.kongtoon.domain.view.model.View;
import com.kongtoon.domain.view.repository.ViewJdbcRepository;
import com.kongtoon.domain.view.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewMapCache implements ViewCache {

    private static final Map<String, Map<String, View>> viewCache = new HashMap<>();

    private final ViewRepository viewRepository;
    private final ViewJdbcRepository viewJdbcRepository;

    @Override
    public void save(User user, Episode episode) {
        String subKey = createKey(user.getLoginId().getIdValue(), episode.getId());
        Map<String, View> viewsForInsert = viewCache.get(INSERT_MAP_MAIN_KEY);
        Map<String, View> viewsForUpdate = viewCache.get(UPDATE_MAP_MAIN_KEY);

        viewRepository.findByUserAndEpisode(user, episode)
                .ifPresentOrElse(
                        view -> saveKeyInUpdateMap(subKey, view, viewsForUpdate),
                        () -> saveKeyInInsertMap(new View(user, episode), subKey, viewsForInsert)
                );
    }

    @Override
    public boolean existsKey(String mainKey, String subKey) {
        return viewCache.get(mainKey)
                .containsKey(subKey);
    }

    @Override
    public List<View> getValues(String mainKey) {
        return viewCache.get(mainKey)
                .values()
                .stream()
                .toList();
    }

    @Override
    public void clear(String mainKey) {
        viewCache.get(mainKey)
                .clear();
    }

    @Override
    public boolean checkInsert() {
        return viewCache.get(INSERT_MAP_MAIN_KEY)
                .size() >= LIMIT_SIZE;
    }

    @Override
    public boolean checkUpdate() {
        return viewCache.get(UPDATE_MAP_MAIN_KEY)
                .size() >= LIMIT_SIZE;
    }

    @Override
    public synchronized void batchInsertToDB() {
        viewJdbcRepository.batchInsert(getValues(ViewCache.INSERT_MAP_MAIN_KEY));
        clear(ViewCache.INSERT_MAP_MAIN_KEY);
    }

    @Override
    public synchronized void batchUpdateToDB() {
        viewJdbcRepository.batchUpdate(getValues(ViewCache.UPDATE_MAP_MAIN_KEY));
        clear(ViewCache.UPDATE_MAP_MAIN_KEY);
    }

    private void saveKeyInInsertMap(View view, String subKey, Map<String, View> viewsForInsert) {
        if (existsKey(INSERT_MAP_MAIN_KEY, subKey)) {
            View findView = viewsForInsert.get(subKey);
            findView.updateLastAccessTime();
        } else {
            viewsForInsert.put(subKey, view);
        }
    }

    private void saveKeyInUpdateMap(String subKey, View view, Map<String, View> viewsForUpdate) {
        view.updateLastAccessTime();
        viewsForUpdate.put(subKey, view);
    }

    @PostConstruct
    private void init() {
        viewCache.put(INSERT_MAP_MAIN_KEY, new ConcurrentHashMap<>());
        viewCache.put(UPDATE_MAP_MAIN_KEY, new ConcurrentHashMap<>());
    }

    @PreDestroy
    private void sendRemainViewDataToStorage() {
        log.info("메모리에 남은 조회 데이터 DB에 삽입");

        viewJdbcRepository.batchInsert(getValues(ViewCache.INSERT_MAP_MAIN_KEY));
        viewJdbcRepository.batchUpdate(getValues(ViewCache.UPDATE_MAP_MAIN_KEY));
    }
}
