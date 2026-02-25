package com.antonyukV516.bot;

import com.antonyukV516.bot.handler.CallbackQueryHandler;
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
import org.telegram.telegrambots.meta.api.objects.User;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UpdateHandler Tests")
class UpdateHandlerTest {

    @Mock
    private CommandDispatcher commandDispatcher;

    @Mock
    private CallbackQueryHandler callbackHandler;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private CallbackQuery callbackQuery;

    @Mock
    private User telegramUser;

    private UpdateHandler updateHandler;

    @BeforeEach
    void setUp() {
        updateHandler = new UpdateHandler(commandDispatcher, callbackHandler);
    }

    @Test
    @DisplayName("✅ Должен передать callback в CallbackQueryHandler")
    void handle_ShouldDispatchToCallbackHandler() {
        // given
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("test_data");
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(callbackHandler.canHandle(update)).thenReturn(true);

        // when
        updateHandler.handle(update);

        // then
        verify(callbackHandler).canHandle(update);
        verify(callbackHandler).handle(update);
        verify(commandDispatcher, never()).dispatch(any());
    }

    @Test
    @DisplayName("✅ Должен передать сообщение в CommandDispatcher")
    void handle_ShouldDispatchToCommandDispatcher() {
        // given
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);

        // when
        updateHandler.handle(update);

        // then
        verify(callbackHandler, never()).handle(any());
        verify(commandDispatcher).dispatch(message);
    }

    @Test
    @DisplayName("✅ Должен игнорировать сообщения без текста")
    void handle_ShouldIgnoreNonTextMessages() {
        // given
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(false);

        // when
        updateHandler.handle(update);

        // then
        verify(commandDispatcher, never()).dispatch(any());
        verify(callbackHandler, never()).handle(any());
    }

    @Test
    @DisplayName("✅ Должен игнорировать обновления без сообщения и без callback")
    void handle_ShouldIgnoreNonMessageUpdates() {
        // given
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(false);

        // when
        updateHandler.handle(update);

        // then
        verify(commandDispatcher, never()).dispatch(any());
        verify(callbackHandler, never()).handle(any());
    }
}