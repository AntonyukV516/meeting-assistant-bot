package com.antonyukV516.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommandDispatcher Tests")
class CommandDispatcherTest {

    @Mock
    private CommandHandler startCommand;

    @Mock
    private CommandHandler unknownCommand;

    @Mock
    private Message message;

    private CommandDispatcher commandDispatcher;
    private final Long TEST_CHAT_ID = 12345L;

    @BeforeEach
    void setUp() {
        commandDispatcher = new CommandDispatcher(
                List.of(startCommand, unknownCommand)
        );

        when(message.getText()).thenReturn("/start");
        when(message.getChatId()).thenReturn(TEST_CHAT_ID);
    }

    @Test
    @DisplayName("✅ Должен найти подходящий CommandHandler и вызвать его")
    void dispatch_ShouldCallMatchingHandler() {
        // given
        when(message.getText()).thenReturn("/start");
        when(startCommand.canHandle("/start", TEST_CHAT_ID)).thenReturn(true);

        // when
        commandDispatcher.dispatch(message);

        // then
        verify(startCommand).handle(message);
        verify(unknownCommand, never()).handle(any());
    }

    @Test
    @DisplayName("✅ Должен вызвать UnknownCommand если нет подходящего хендлера")
    void dispatch_ShouldCallUnknownCommand_WhenNoHandlerFound() {
        // given
        when(message.getText()).thenReturn("/unknown");
        when(startCommand.canHandle("/unknown", TEST_CHAT_ID)).thenReturn(false);
        when(unknownCommand.canHandle("/unknown", TEST_CHAT_ID)).thenReturn(true);

        // when
        commandDispatcher.dispatch(message);

        // then
        verify(startCommand, never()).handle(any());
        verify(unknownCommand).handle(message);
    }

    @Test
    @DisplayName("✅ Должен пройти по всем командам в порядке списка")
    void dispatch_ShouldIterateThroughCommandsInOrder() {
        // given
        when(message.getText()).thenReturn("/test");
        when(startCommand.canHandle("/test", TEST_CHAT_ID)).thenReturn(false);
        when(unknownCommand.canHandle("/test", TEST_CHAT_ID)).thenReturn(true);

        // when
        commandDispatcher.dispatch(message);

        // then
        verify(startCommand).canHandle("/test", TEST_CHAT_ID);
        verify(unknownCommand).canHandle("/test", TEST_CHAT_ID);
        verify(unknownCommand).handle(message);
    }
}