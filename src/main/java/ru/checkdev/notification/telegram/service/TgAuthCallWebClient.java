package ru.checkdev.notification.telegram.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.service.Retry;

/**
 * Класс реализует методы get и post для отправки сообщений через WebClient
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@org.springframework.context.annotation.Profile("default")
@Service
@NoArgsConstructor
@Slf4j
public class TgAuthCallWebClient implements TgCall {

    @Value("${server.auth}")
    private String urlServiceAuth;

    @Value("${retry.retries:3}")
    private int retries;

    @Value("${retry.delay:1000}")
    private long delay;

    private WebClient webClient;

    public TgAuthCallWebClient(String urlServiceAuth, int retries, long delay) {
        this.urlServiceAuth = urlServiceAuth;
        this.retries = retries;
        this.delay = delay;
    }

    public TgAuthCallWebClient(WebClient webClient, int retries, long delay) {
        this.webClient = webClient;
        this.retries = retries;
        this.delay = delay;
    }

    private WebClient webClient() {
        return webClient != null ? webClient : WebClient.create(urlServiceAuth);
    }

    private Retry retry() {
        return new Retry(retries, delay);
    }

    /**
     * Выполняет GET-запрос к сервису авторизации.
     *
     * @param uri URI запроса
     * @return Mono<Profile>
     */
    @Override
    public Mono<Profile> doGet(String uri) {
        return Mono.fromCallable(() -> retry().execute(
                () -> webClient()
                        .get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(Profile.class)
                        .block(), null)
        );
    }

    /**
     * Выполняет POST-запрос.
     *
     * @param uri URI запроса
     * @param profile тело запроса
     * @return ответ сервиса
     */
    @Override
    public Mono<Object> doPost(String uri, Profile profile) {
        return Mono.fromCallable(() -> retry().execute(
                () -> webClient()
                        .post()
                        .uri(uri)
                        .bodyValue(profile)
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block(), null)
        );
    }

    @Override
    public Mono<Object> doPost(String uri) {
        return Mono.fromCallable(() -> retry().execute(
                () -> webClient()
                        .post()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block(), null)
        );
    }
}
