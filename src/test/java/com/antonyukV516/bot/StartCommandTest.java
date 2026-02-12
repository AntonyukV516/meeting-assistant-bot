package com.antonyukV516.bot;

import com.antonyukV516.model.TelegramUser;
import com.antonyukV516.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StartCommand Tests")
class StartCommandTest {

    @Mock
    private UserService userService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private StartCommand startCommand;

    @Mock
    private Message message;

    @Mock
    private User telegramUser;

    private final Long CHAT_ID = 123456789L;
    private final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getFrom()).thenReturn(telegramUser);
        when(telegramUser.getUserName()).thenReturn(USERNAME);
    }

    @Test
    @DisplayName("✅ Должен распознать команду /start")
    void canHandle_ShouldReturnTrue_ForStartCommand() {
        assertThat(startCommand.canHandle("/start")).isTrue();
        assertThat(startCommand.canHandle("/start something")).isFalse();
        assertThat(startCommand.canHandle("/new")).isFalse();
    }

    @Test
    @DisplayName("✅ Должен зарегистрировать пользователя и отправить приветствие")
    void handle_ShouldRegisterUserAndSendWelcome() {
        startCommand.handle(message);

        ArgumentCaptor<TelegramUser> userCaptor = ArgumentCaptor.forClass(TelegramUser.class);
        verify(userService).findOrCreateUser(userCaptor.capture(), eq(CHAT_ID));

        TelegramUser capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUserName()).isEqualTo(USERNAME);

        verify(messageSender).sendMessage(eq(CHAT_ID),
                argThat(text -> text.contains("Привет") && text.contains(USERNAME)));
    }

    @Test
    @DisplayName("✅ Должен обработать ошибку сервиса")
    void handle_ShouldHandleServiceError() {
        when(userService.findOrCreateUser(any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        startCommand.handle(message);

        verify(messageSender).sendMessage(eq(CHAT_ID),
                argThat(text -> text.contains("Произошла ошибка")));
    }
}