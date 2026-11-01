package com.mb.modulith.localstack.scheduler;

import com.mb.modulith.localstack.contants.Constants;
import com.mb.modulith.localstack.data.model.TempTable3;
import com.mb.modulith.localstack.service.TempTableService;
import com.mb.modulith.localstack.service.impl.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempTableScheduleJob2 {

    private final TempTableService tempTableService;
    private final MetricService metricService;
    private final ReentrantLock lock = new ReentrantLock(true);

    @Scheduled(fixedRate = 86400000, initialDelay = 10000, zone = "Europe/Berlin")
    public void scheduleTempTableDeletion() {
        log.info("Schedule TempTable deletion job is started.");

        // If the lock is held by another thread then this method will return immediately with the value
        // false.
        if (lock.tryLock()) {
            lock.lock();
            Instant nDaysAgo = Instant.now().minus(Duration.ofDays(7));
            try {
                tempTableService.createTable();

                processPublisher(tempTableService.getRecords(), nDaysAgo);
            } catch (Exception e) {
                log.error("Exception occurred while scheduleTempTableDeletion job is running. Please check README.md. Exception: {}", ExceptionUtils.getStackTrace(e));
                metricService.count(Constants.START_SCHEDULE_JOB);
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            log.info("Schedule TempTable deletion job is completed.");
        } else {
            log.info("Schedule TempTable deletion job is already processing and locked.");
        }
    }

    private void processPublisher(PagePublisher<TempTable3> publisher, Instant nDaysAgo) {
        // A Subscription represents a one-to-one life-cycle of a Subscriber subscribing to a Publisher.
        publisher.subscribe(
                new Subscriber<>() {

                    // Maintain a reference to the subscription object, which is required to request data from
                    // the publisher.
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        // Request method should be called to demand data. Here we request a single page.
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(Page<TempTable3> page) {
                        List<TempTable3> items = page.items();

                        metricService.count(Constants.START_SCHEDULE_JOB, items.size(), Constants.SIZE_TAG, String.valueOf(items.size()));

                        List<TempTable3> inactiveTempTables = items
                                .stream()
                                .filter(tempTable ->
                                        "Inactive".equalsIgnoreCase(tempTable.getColor())
                                                && StringUtils.isNotBlank(tempTable.getId())
                                                && Instant.parse(tempTable.getId()).isBefore(nDaysAgo))
                                .toList();

                        metricService.count(Constants.START_SCHEDULE_JOB, inactiveTempTables.size(), Constants.SIZE_TAG, String.valueOf(inactiveTempTables.size()));

                        List<Key> keys = tempTableService.deleteRecords(inactiveTempTables);

                        if (!keys.isEmpty()) {
                            keys.forEach(key -> log.error("unprocessedDeleteItemsForTable - key: {}", key));
                            metricService.count(Constants.START_SCHEDULE_JOB, keys.size(), Constants.EXCEPTION_TAG, keys.stream().map(key -> key.partitionKeyValue().s()).collect(Collectors.joining()));
                        }

                        // After you process the current page, call the request method to signal that you are
                        // ready for next page.
                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // Called when an error has occurred while processing the requests.
                        log.error("Exception occurred while processing publisher. Exception: {}", ExceptionUtils.getStackTrace(throwable));
                        metricService.count(Constants.START_SCHEDULE_JOB);
                    }

                    @Override
                    public void onComplete() {
                        // This indicates all the results are delivered and there are no more pages left.
                        log.info("All the results are delivered and there are no more pages left.");
                    }
                });
    }
}
