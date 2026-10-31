package com.mb.modulith.localstack.scheduler;

import com.mb.modulith.localstack.data.model.TempTable;
import com.mb.modulith.localstack.service.TempTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempTableScheduleJob {

    private final TempTableService tempTableService;
    private final ReentrantLock lock = new ReentrantLock(true);

    @Scheduled(fixedRate = 86400000, zone = "Europe/Istanbul")
    public void scheduleTempTableDeletion() {
        log.info("Schedule TempTable deletion job is started.");
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            log.info("Schedule TempTable deletion job is locked.");
            return;
        }

        lock.lock();
        try {
            tempTableService.createTableIfNotExists();
            tempTableService.loadSampleItems();

            Iterator<TempTable> iterator = tempTableService.getTempTableRecords().iterator();

            if (!iterator.hasNext()) {
                throw new NoSuchElementException("TempTable not found!");
            }

            while (iterator.hasNext()) {
                TempTable row = iterator.next();
                log.info("TempTable value: {}", row);
            }
        } catch (Exception e) {
            log.error("Exception occurred while scheduleTempTableDeletion job is running. Please check README.md. Exception: {}", ExceptionUtils.getStackTrace(e));
        } finally {
            lock.unlock();
        }
        log.info("Schedule TempTable deletion job is completed.");
    }
}
