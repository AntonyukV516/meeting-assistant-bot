package com.antonyukV516.bot.command;

import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.bot.state.PendingMeeting;
import com.antonyukV516.bot.state.UserState;
import com.antonyukV516.bot.state.UserStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NewMeetingCommand Tests")
class NewMeetingCommandTest {

    @Mock
    private UserStateService stateService;

    @Mock
    private KeyboardFactory keyboardFactory;

    @InjectMocks
    private NewMeetingCommand newMeetingCommand;

    @Mock
    private Message message;

    @Mock
    private User telegramUser;

    private final Long CHAT_ID = 12345L;
    private final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getFrom()).thenReturn(telegramUser);
        when(telegramUser.getUserName()).thenReturn(USERNAME);
    }

    @Test
    @DisplayName("✅ canHandle должен возвращать true для /new и кнопки")
    void canHandle_ShouldReturnTrue_ForNewCommand() {
        assertThat(newMeetingCommand.canHandle("/new", CHAT_ID)).isTrue();
        assertThat(newMeetingCommand.canHandle("/new_meeting", CHAT_ID)).isTrue();
        assertThat(newMeetingCommand.canHandle("📝 Создать встречу", CHAT_ID)).isTrue();
        assertThat(newMeetingCommand.canHandle("/start", CHAT_ID)).isFalse();
    }

    @Test
    @DisplayName("✅ handle должен начинать создание встречи")
    void handle_ShouldStartMeetingCreation() {
        // given
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(false);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            newMeetingCommand.handle(message);

            // then
            ArgumentCaptor<PendingMeeting> pendingCaptor =
                    ArgumentCaptor.forClass(PendingMeeting.class);

            verify(stateService).setPendingMeeting(eq(CHAT_ID), pendingCaptor.capture());
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CREATING_MEETING_TITLE));

            PendingMeeting pending = pendingCaptor.getValue();
            assertThat(pending.getCreatorUsername()).isEqualTo(USERNAME);
            assertThat(pending.getChatId()).isEqualTo(CHAT_ID);

            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), anyString())
            );
        }
    }

    @Test
    @DisplayName("✅ handle должен предупреждать, если пользователь уже в диалоге")
    void handle_ShouldWarn_WhenAlreadyInDialog() {
        // given
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            newMeetingCommand.handle(message);

            // then
            verify(stateService, never()).setPendingMeeting(any(), any());
            verify(stateService, never()).setState(any(), any());

            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID),
                            argThat(text -> text.contains("уже создаете встречу"))
                    )
            );
        }
    }
}