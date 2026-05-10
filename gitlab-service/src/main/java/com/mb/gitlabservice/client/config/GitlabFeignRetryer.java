package com.mb.gitlabservice.client.config;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class GitlabFeignRetryer implements Retryer {

    private final int maxAttempts;
    private final long backoff;
    private int attempt = 1;

    public GitlabFeignRetryer() {
        this(2, 1000);
    }

    public GitlabFeignRetryer(int maxAttempts, long backoff) {
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
    }

    public void continueOrPropagate(RetryableException e) {
        log.info("Retrying: {} attempt {}", e.request().url(), attempt);

        if (attempt++ >= maxAttempts) {
            log.info("Gitlab retry attempt is exceeded");
            return;
        }

        try {
            TimeUnit.MILLISECONDS.sleep(backoff);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates a new instance for each request — required by Feign's {@link Retryer} interface.
     * Uses a copy factory instead of {@code super.clone()} to satisfy both the interface
     * contract and static analysis rules (SonarQube S2975).
     */
    @Override
    @SuppressWarnings({"squid:S2975", "java:S2975", "MethodDoesntCallSuperMethod"})
    public Retryer clone() { // NOSONAR - required by Feign Retryer interface; intentionally uses copy factory
        return new GitlabFeignRetryer(maxAttempts, backoff);
    }
}
