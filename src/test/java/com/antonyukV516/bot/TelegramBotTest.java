package com.antonyukV516.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelegramBot Routing Tests")
class TelegramBotTest {

    @Mock
    private CommandHandler startCommand;

    @Mock
    private CommandHandler unknownCommand;

    @InjectMocks
    private TelegramBot telegramBot;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private User telegramUser;

    @BeforeEach
    void setUp() {
        telegramBot = new TelegramBot("token", "bot",
                List.of(startCommand, unknownCommand));

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
    }

    @Test
    @DisplayName("✅ Должен направить /start в StartCommand")
    void onUpdateReceived_ShouldRouteStartCommand() {
        User mockUser = mock(User.class);
        when(message.getFrom()).thenReturn(mockUser);
        when(mockUser.getUserName()).thenReturn("testuser");
        when(message.getText()).thenReturn("/start");
        when(startCommand.canHandle("/start")).thenReturn(true);

        telegramBot.onUpdateReceived(update);

        verify(startCommand).handle(message);
        verify(unknownCommand, never()).handle(any());
    }

    @Test
    @DisplayName("✅ Должен направить неизвестную команду в UnknownCommand")
    void onUpdateReceived_ShouldRouteUnknownCommand() {
        User mockUser = mock(User.class);
        when(message.getFrom()).thenReturn(mockUser);
        when(mockUser.getUserName()).thenReturn("testuser");
        when(message.getText()).thenReturn("/unknown");
        when(startCommand.canHandle("/unknown")).thenReturn(false);
        when(unknownCommand.canHandle("/unknown")).thenReturn(true);

        telegramBot.onUpdateReceived(update);

        verify(startCommand, never()).handle(any());
        verify(unknownCommand).handle(message);
    }

    @Test
    @DisplayName("✅ Должен игнорировать сообщения без текста")
    void onUpdateReceived_ShouldIgnoreNonTextMessages() {
        when(message.hasText()).thenReturn(false);

        telegramBot.onUpdateReceived(update);

        verifyNoInteractions(startCommand, unknownCommand);
    }
}