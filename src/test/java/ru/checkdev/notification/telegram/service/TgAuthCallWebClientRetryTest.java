package ru.checkdev.notification.telegram.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TgAuthCallWebClientRetryTest {

    @Test
    void whenAuthGetFailsTemporarilyThenRetryReturnsProfile() {
        var attempts = new AtomicInteger();
        var profile = new Profile();
        profile.setId(1);
        profile.setUsername("User");
        var webClient = mock(WebClient.class);
        var uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        var headersSpec = mock(WebClient.RequestHeadersSpec.class);
        var responseSpec = mock(WebClient.ResponseSpec.class);
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri("/profiles/tg/1")).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Profile.class)).thenAnswer(invocation -> {
            if (attempts.incrementAndGet() < 3) {
                return Mono.error(new IllegalStateException("temporary error"));
            }
            return Mono.just(profile);
        });
        var client = new TgAuthCallWebClient(webClient, 3, 0, 5000);

        Profile result = client.doGet("/profiles/tg/1").block();

        assertThat(result).isEqualTo(profile);
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void whenAuthPostFailsTemporarilyThenRetryReturnsObject() {
        var attempts = new AtomicInteger();
        var profile = new Profile();
        profile.setEmail("user@mail.ru");
        var webClient = mock(WebClient.class);
        var uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        var bodySpec = mock(WebClient.RequestBodySpec.class);
        var headersSpec = mock(WebClient.RequestHeadersSpec.class);
        var responseSpec = mock(WebClient.ResponseSpec.class);
        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("/profiles/tg/byEmailAndPassword")).thenReturn(bodySpec);
        when(bodySpec.bodyValue(profile)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Object.class)).thenAnswer(invocation -> {
            if (attempts.incrementAndGet() < 2) {
                return Mono.error(new IllegalStateException("temporary error"));
            }
            return Mono.just(profile);
        });
        var client = new TgAuthCallWebClient(webClient, 3, 0, 5000);

        Object result = client.doPost("/profiles/tg/byEmailAndPassword", profile).block();

        assertThat(result).isEqualTo(profile);
        assertThat(attempts.get()).isEqualTo(2);
    }
}
