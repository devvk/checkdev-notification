package ru.checkdev.notification.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RetryTest {

    @Test
    void whenActionFailsTwiceThenReturnsResultOnThirdAttempt() {
        var attempts = new AtomicInteger();
        var retry = new Retry(3, 0);

        var result = retry.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("temporary error");
            }
            return "success";
        }, "default");

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void whenAllAttemptsFailThenReturnsDefaultValue() {
        var attempts = new AtomicInteger();
        var retry = new Retry(3, 0);

        var result = retry.execute(() -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("temporary error");
        }, "default");

        assertThat(result).isEqualTo("default");
        assertThat(attempts.get()).isEqualTo(3);
    }
}