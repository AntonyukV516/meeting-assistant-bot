package com.antonyukV516.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private CallbackQuery callbackQuery;

    private TelegramBot telegramBot;

    @BeforeEach
    void setUp() {
        telegramBot = new TelegramBot("token", "bot", updateHandler);
    }

    @Test
    @DisplayName("✅ Должен передать callback в UpdateHandler")
    void onUpdateReceived_ShouldPassCallbackToUpdateHandler() {
        // given
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("test_data");

        // when
        telegramBot.onUpdateReceived(update);

        // then
        verify(updateHandler).handle(update);
    }

    @Test
    @DisplayName("✅ Должен передать сообщение в UpdateHandler")
    void onUpdateReceived_ShouldPassMessageToUpdateHandler() {
        // given
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);

        // when
        telegramBot.onUpdateReceived(update);

        // then
        verify(updateHandler).handle(update);
    }

    @Test
    @DisplayName("✅ Не должен игнорировать сообщения без текста")
    void onUpdateReceived_ShouldStillPassToUpdateHandler_EvenWithoutText() {
        // given
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(false);

        // when
        telegramBot.onUpdateReceived(update);

        // then
        verify(updateHandler).handle(update);
    }

    @Test
    @DisplayName("✅ Не должен игнорировать обновления без сообщения")
    void onUpdateReceived_ShouldStillPassToUpdateHandler_EvenWithoutMessage() {
        // given
        when(update.hasMessage()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(false);

        // when
        telegramBot.onUpdateReceived(update);

        // then
        verify(updateHandler).handle(update);
    }
}