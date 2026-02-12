package com.antonyukV516.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateHandler Tests")
class UpdateHandlerTest {

    @Mock
    private CommandDispatcher commandDispatcher;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private User telegramUser;

    private UpdateHandler updateHandler;

    @BeforeEach
    void setUp() {
        updateHandler = new UpdateHandler(commandDispatcher);
    }

    @Test
    @DisplayName("✅ Должен передать сообщение в CommandDispatcher")
    void handle_ShouldDispatchToCommandDispatcher() {
        // given
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/start");
        when(message.getFrom()).thenReturn(telegramUser);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(message.getChatId()).thenReturn(123L);

        // when
        updateHandler.handle(update);

        // then
        verify(commandDispatcher).dispatch(message);
    }

    @Test
    @DisplayName("✅ Должен игнорировать сообщения без текста")
    void handle_ShouldIgnoreNonTextMessages() {
        // given
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(false);

        // when
        updateHandler.handle(update);

        // then
        verify(commandDispatcher, never()).dispatch(any());
    }

    @Test
    @DisplayName("✅ Должен игнорировать обновления без сообщения")
    void handle_ShouldIgnoreNonMessageUpdates() {
        // given
        when(update.hasMessage()).thenReturn(false);

        // when
        updateHandler.handle(update);

        // then
        verify(commandDispatcher, never()).dispatch(any());
    }
}
