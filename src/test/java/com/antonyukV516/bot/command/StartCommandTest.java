package com.antonyukV516.bot.command;

import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.model.TelegramUser;
import com.antonyukV516.model.User;
import com.antonyukV516.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StartCommand Tests")
class StartCommandTest {

    @Mock
    private UserService userService;

    @Mock
    private KeyboardFactory keyboardFactory;

    @InjectMocks
    private StartCommand startCommand;

    @Mock
    private Message message;

    @Mock
    private org.telegram.telegrambots.meta.api.objects.User telegramApiUser;

    @Mock
    private ReplyKeyboardMarkup mockKeyboard;

    private final Long CHAT_ID = 12345L;
    private final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getFrom()).thenReturn(telegramApiUser);
        when(telegramApiUser.getUserName()).thenReturn(USERNAME);
    }


    @Test
    @DisplayName("✅ canHandle должен возвращать true для /start")
    void canHandle_ShouldReturnTrue_ForStartCommand() {
        boolean result = startCommand.canHandle("/start", CHAT_ID);
        assertThat(result).isTrue();

        result = startCommand.canHandle("/start something", CHAT_ID);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("✅ handle должен регистрировать нового пользователя")
    void handle_ShouldRegisterNewUser() {
        // given
        when(userService.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(keyboardFactory.createMainMenu()).thenReturn(mockKeyboard);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            startCommand.handle(message);

            // then
            verify(userService).findOrCreateUser(any(TelegramUser.class), eq(CHAT_ID));
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handle должен приветствовать существующего пользователя")
    void handle_ShouldWelcomeExistingUser() {
        // given
        User existingUser = User.builder()
                .telegramUsername(USERNAME)
                .chatId(CHAT_ID)
                .build();
        when(userService.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));
        when(keyboardFactory.createMainMenu()).thenReturn(mockKeyboard);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            startCommand.handle(message);

            // then
            verify(userService, never()).findOrCreateUser(any(), any());
            verify(userService, never()).save(any());
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID),
                            argThat(text -> text.contains("С возвращением")),
                            eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handle должен обновлять chatId если он изменился")
    void handle_ShouldUpdateChatId_WhenChanged() {
        // given
        User existingUser = User.builder()
                .telegramUsername(USERNAME)
                .chatId(99999L) // другой chatId
                .build();
        when(userService.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));
        when(keyboardFactory.createMainMenu()).thenReturn(mockKeyboard);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            startCommand.handle(message);

            // then
            verify(userService).save(existingUser);
            assertThat(existingUser.getChatId()).isEqualTo(CHAT_ID);
        }
    }
}