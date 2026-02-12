package com.antonyukV516.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TelegramBot Routing Tests")
class TelegramBotTest {

    @Mock
    private UpdateHandler updateHandler;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private User telegramUser;

    private TelegramBot telegramBot;

    @BeforeEach
    void setUp() {
        telegramBot = new TelegramBot("token", "bot", updateHandler);
    }

    @Test
    @DisplayName("✅ Должен передать обновление в UpdateHandler")
    void onUpdateReceived_ShouldDispatchToUpdateHandler() {
        // given
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/start");
        when(message.getFrom()).thenReturn(telegramUser);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(message.getChatId()).thenReturn(123L);

        // when
        telegramBot.onUpdateReceived(update);

        // then
        verify(updateHandler).handle(update);
    }

    @Test
    @DisplayName("✅ Должен игнорировать сообщения без текста")
    void onUpdateReceived_ShouldIgnoreNonTextMessages() {
        // given
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(false);

        // when
        telegramBot.onUpdateReceived(update);

        // then
        verify(updateHandler, never()).handle(any());
    }

    @Test
    @DisplayName("✅ Должен игнорировать обновления без сообщения")
    void onUpdateReceived_ShouldIgnoreNonMessageUpdates() {
        // given
        when(update.hasMessage()).thenReturn(false);

        // when
        telegramBot.onUpdateReceived(update);

        // then
        verify(updateHandler, never()).handle(any());
    }
}