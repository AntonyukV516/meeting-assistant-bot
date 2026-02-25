package com.antonyukV516.bot;

import com.antonyukV516.bot.command.StartCommand;
import com.antonyukV516.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {

    @Mock
    private UserService userService;

    @Mock
    private Message message;

    @Mock
    private User telegramUser;

    @InjectMocks
    private StartCommand startCommand;

    private final Long CHAT_ID = 123L;
    private final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getFrom()).thenReturn(telegramUser);
        when(telegramUser.getUserName()).thenReturn(USERNAME);
    }

    @Test
    void handle_ShouldRegisterUser() {
        startCommand.handle(message);
        verify(userService).findOrCreateUser(any(), eq(CHAT_ID));
    }
}