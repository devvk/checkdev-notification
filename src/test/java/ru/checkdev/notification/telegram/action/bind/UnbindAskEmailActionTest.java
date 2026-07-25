package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;

import static org.assertj.core.api.Assertions.assertThat;

class UnbindAskEmailActionTest {
    private static final Chat CHAT = new Chat(1L, "type");

    private UserTelegramService userTelegramService;
    private UnbindAskEmailAction action;
    private Update update;
    private Message message;

    @BeforeEach
    void setUp() {
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        action = new UnbindAskEmailAction(userTelegramService);
        update = new Update();
        message = new Message();
        message.setChat(CHAT);
        update.setMessage(message);
    }

    @Test
    void whenTelegramAccountIsBoundThenAskEmail() {
        userTelegramService.save(new UserTelegram(0, 100, CHAT.getId(), false));

        BotApiMethod botApiMethod = action.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(sendMessage.getText()).isEqualTo("Введите email пользователя:");
    }

    @Test
    void whenTelegramAccountIsNotBoundThenReturnMessage() {
        BotApiMethod botApiMethod = action.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(sendMessage.getText())
                .isEqualTo("К данному аккаунту телеграм не привязан аккаунт CheckDev");
    }
}
