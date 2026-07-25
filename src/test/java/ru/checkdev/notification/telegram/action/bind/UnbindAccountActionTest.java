package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.dto.ProfileTgDTO;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.service.TgCall;

import static org.assertj.core.api.Assertions.assertThat;

class UnbindAccountActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private UserTelegramService userTelegramService;
    private SessionTg sessionTg;
    private UnbindAccountAction unbindAccountAction;
    private Update update;
    private Message message;

    @BeforeEach
    void setUp() {
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        sessionTg = new SessionTg();
        unbindAccountAction = new UnbindAccountAction(
                sessionTg, new SuccessTgCall(), userTelegramService);
        update = new Update();
        message = new Message();
    }

    @Test
    void whenUnbindWithUserTelegramThenOk() {
        message.setChat(CHAT);
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "email@email.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "password");
        UserTelegram userTelegram = new UserTelegram(0, 100, 1L, false);
        userTelegramService.save(userTelegram);
        String expectMessage = "Ваш аккаунт CheckDev отвязан от текущего аккаунта Telegram";

        BotApiMethod botApiMethod = unbindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(userTelegramService.findByChatId(1L)).isEmpty();
        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    @Test
    void whenUnbindWithAnotherAccountThenMessageCredentialsDoNotMatch() {
        message.setChat(CHAT);
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "email@email.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "password");
        UserTelegram userTelegram = new UserTelegram(0, 200, 1L, false);
        userTelegramService.save(userTelegram);
        String expectMessage = "Введенные данные не соответствуют привязанному аккаунту";

        BotApiMethod botApiMethod = unbindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(userTelegramService.findByChatId(1L)).isPresent();
        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    @Test
    void whenUnbindWithoutUserTelegramThenMessageAccountIsNotBind() {
        message.setChat(CHAT);
        update.setMessage(message);
        String expectMessage = "К данному аккаунту телеграм не привязан аккаунт CheckDev";

        BotApiMethod botApiMethod = unbindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    private static class SuccessTgCall implements TgCall {
        @Override
        public Mono<Profile> doGet(String url) {
            return Mono.empty();
        }

        @Override
        public Mono<Object> doPost(String url, Profile profile) {
            return Mono.just(new ProfileTgDTO(100, "username", profile.getEmail()));
        }

        @Override
        public Mono<Object> doPost(String url) {
            return Mono.empty();
        }
    }

}