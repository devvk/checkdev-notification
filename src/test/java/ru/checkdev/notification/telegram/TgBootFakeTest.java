package ru.checkdev.notification.telegram;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.telegram.action.Action;
import ru.checkdev.notification.telegram.action.info.InfoAction;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TgBootFakeTest {
    private static final Chat CHAT = new Chat(1L, "type");

    @Test
    void whenFirstMessageIsUnknownCommandThenReturnUnknownCommandMessage() {
        var bot = new CapturingTgBootFake(Map.of(
                "/start", List.of(new InfoAction(List.of("/start - Доступные команды")))));

        bot.onUpdateReceived(update("/unknown"));

        SendMessage message = (SendMessage) bot.sent;
        assertThat(message.getText())
                .isEqualTo("Команда не поддерживается! Список доступных команд: /start");
    }

    @Test
    void whenStartCommandThenReturnCommands() {
        var bot = new CapturingTgBootFake(Map.of(
                "/start", List.of(new InfoAction(List.of(
                        "/start - Доступные команды",
                        "/new - Регистрация нового пользователя",
                        "/check - Связанный аккаунт",
                        "/bind - Привязать аккаунт CheckDev",
                        "/unbind - Отвязать аккаунт CheckDev")))));

        bot.onUpdateReceived(update("/start"));

        SendMessage message = (SendMessage) bot.sent;
        assertThat(message.getText())
                .contains("/start", "/new", "/check", "/bind", "/unbind");
    }

    private static Update update(String text) {
        var update = new Update();
        var message = new Message();
        message.setChat(CHAT);
        message.setText(text);
        update.setMessage(message);
        return update;
    }

    private static class CapturingTgBootFake extends TgBootFake {
        private BotApiMethod sent;

        private CapturingTgBootFake(Map<String, List<Action>> actions) {
            super(actions, "username", "token");
        }

        @Override
        public void send(BotApiMethod msg) {
            this.sent = msg;
        }
    }
}
