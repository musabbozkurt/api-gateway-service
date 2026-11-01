package com.mb.modulith.localstack.scheduler;

import com.mb.modulith.localstack.constants.Constants;
import com.mb.modulith.localstack.data.model.TempTable3;
import com.mb.modulith.localstack.service.TempTableService;
import com.mb.modulith.localstack.service.impl.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempTableScheduleJob4FluxSinksMany {

    private final TempTableService tempTableService;
    private final MetricService metricService;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Sinks.Many<Object> cancellationSinks = Sinks.many().multicast().directAllOrNothing();

    @Scheduled(fixedRate = 86400000, initialDelay = 10000, zone = "Europe/Berlin")
    public void scheduleTempTableDeletion() {
        log.info("Schedule TempTable deletion job is started.");
        runTempTableDeletion().subscribe();
        // Async part should be completed before fetching the latest result.
        runTempTableDeletion().block(Duration.ofSeconds(5));
    }

    private Mono<SignalType> runTempTableDeletion() {
        Sinks.One<SignalType> doneSink = Sinks.one();

        // If the lock is held by another thread then this method will return immediately with the value
        // false.
        if (lock.tryLock()) {
            try {
                tempTableService.createTable();

                processPublisher(tempTableService.getRecords())
                        .doFinally(doneSink::tryEmitValue)
                        .subscribe();
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
        return doneSink.asMono();
    }

    private Flux<List<Key>> processPublisher(PagePublisher<TempTable3> publisher) {
        return Flux.from(publisher)
                .map(Page::items)
                .doOnNext(tempTable3s -> metricService.count(Constants.SIZE, tempTable3s.size()))
                .flatMap(Flux::fromIterable)
                .filter(this::isEligibleForHardDeletion)
                .doOnNext(_ -> metricService.count(Constants.SIZE, 1))
                .buffer(25)
                .flatMap(tempTable3s -> Mono.fromFuture(tempTableService.deleteAllAsync(tempTable3s)))
                .takeUntilOther(cancellationSinks.asFlux())
                .doOnNext(key -> {
                    log.error("unprocessedDeleteItemsForTable - key: {}", key);
                    metricService.count(Constants.UNPROCESSED_DELETE_ITEMS_FOR_TABLE, 1);
                })
                .doOnError(throwable -> {
                    // Called when an error has occurred while processing the requests.
                    log.error("Exception occurred while processing publisher. Exception: {}", ExceptionUtils.getStackTrace(throwable));
                    metricService.count(Constants.FAILED_TO_PROCESS_PUBLISHER);
                })
                .doOnComplete(() -> log.info("All the results are delivered and there are no more pages left."))
                .doFinally(_ -> {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                });
    }

    private boolean isEligibleForHardDeletion(TempTable3 tempTable3) {
        return StringUtils.isNotBlank(tempTable3.getColor());
    }

    public void cancelScheduleTempTableDeletion(Object cancellationValue) {
        cancellationSinks.tryEmitNext(cancellationValue);
    }
}
