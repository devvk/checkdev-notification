package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class Retry {

    private final int retries;
    private final long delay;

    @FunctionalInterface
    public interface Action<T> {
        T execute() throws Exception;
    }

    public <R> R execute(Action<R> action, R defaultValue) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                return action.execute();
            } catch (Exception e) {
                lastException = e;
                log.error("Attempt {}/{} failed", attempt, retries, e);
                if (attempt < retries) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted while waiting for retry attempt {}", attempt, interruptedException);
                        return defaultValue;
                    }
                }
            }
        }
        log.error("All {} attempts failed. Returning default value.", retries, lastException);
        return defaultValue;
    }
}
