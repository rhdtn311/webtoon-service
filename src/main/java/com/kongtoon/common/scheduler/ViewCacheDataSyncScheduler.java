package com.kongtoon.common.scheduler;

import com.kongtoon.domain.view.repository.cache.ViewCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ViewCacheDataSyncScheduler {

    private final ViewCache viewCache;

    @Transactional
    @Scheduled(cron = "0 */30 * * * *")
    public void batchInsertAndUpdateToDB() {
        log.info("ViewCache 데이터 DB 반영 스케줄링 시작");

        viewCache.batchInsertToDB();
        viewCache.batchUpdateToDB();

        log.info("ViewCache 데이터 DB 반영 스케줄링 종료");
    }
}
