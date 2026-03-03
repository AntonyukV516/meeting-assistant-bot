package com.antonyukV516.bot.handler;

import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.bot.state.PendingMeeting;
import com.antonyukV516.bot.state.UserState;
import com.antonyukV516.bot.state.UserStateService;
import com.antonyukV516.model.Meeting;
import com.antonyukV516.service.MeetingService;
import com.antonyukV516.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MeetingCreationHandler Tests")
class MeetingCreationHandlerTest {

    @Mock
    private UserStateService stateService;

    @Mock
    private MeetingService meetingService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private KeyboardFactory keyboardFactory;

    @InjectMocks
    private MeetingCreationHandler meetingCreationHandler;

    @Mock
    private Message message;

    @Mock
    private User telegramUser;

    @Mock
    private ReplyKeyboardMarkup mockKeyboard;

    private final Long CHAT_ID = 12345L;
    private final String USERNAME = "testuser";

    private PendingMeeting pendingMeeting;

    @BeforeEach
    void setUp() {
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getFrom()).thenReturn(telegramUser);
        when(telegramUser.getUserName()).thenReturn(USERNAME);

        pendingMeeting = PendingMeeting.builder()
                .chatId(CHAT_ID)
                .creatorUsername(USERNAME)
                .title("Test Meeting")
                .description("Test Description")
                .tags(new ArrayList<>())
                .dateTime(LocalDateTime.now().plusDays(1))
                .location("Test Location")
                .maxPeople(5)
                .build();

        when(stateService.getPendingMeeting(CHAT_ID)).thenReturn(pendingMeeting);

        // ✅ Добавляем все необходимые моки
        when(keyboardFactory.createSkipKeyboard()).thenReturn(mockKeyboard);
        when(keyboardFactory.createConfirmationKeyboard()).thenReturn(mockKeyboard);
        when(keyboardFactory.createMainMenu()).thenReturn(mockKeyboard);
        when(keyboardFactory.createTagSelectionKeyboard(anySet()))
                .thenReturn(mock(InlineKeyboardMarkup.class));

        // ✅ Мок для MeetingService
        Meeting mockMeeting = mock(Meeting.class);
        when(mockMeeting.getTitle()).thenReturn("Test Meeting");
        when(mockMeeting.getDateTime()).thenReturn(LocalDateTime.now());
        when(mockMeeting.getLocation()).thenReturn("Test Location");
        when(meetingService.createMeeting(anyString(), any())).thenReturn(mockMeeting);
    }

    // ===================== БАЗОВЫЕ ТЕСТЫ =====================

    @Test
    @DisplayName("✅ canHandle должен возвращать true, если пользователь создает встречу")
    void canHandle_ShouldReturnTrue_WhenUserIsCreatingMeeting() {
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);

        boolean result = meetingCreationHandler.canHandle("/any", CHAT_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("✅ canHandle должен возвращать false, если пользователь не создает встречу")
    void canHandle_ShouldReturnFalse_WhenUserIsNotCreatingMeeting() {
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(false);

        boolean result = meetingCreationHandler.canHandle("/any", CHAT_ID);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("✅ handle должен обрабатывать отмену")
    void handle_ShouldCancelCreation() {
        when(message.getText()).thenReturn("❌ Отмена");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_TITLE);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService).resetState(CHAT_ID);
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handle должен обрабатывать случай с потерянным PendingMeeting")
    void handle_ShouldHandleMissingPendingMeeting() {
        when(message.getText()).thenReturn("test");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_TITLE);
        when(stateService.getPendingMeeting(CHAT_ID)).thenReturn(null);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService).resetState(CHAT_ID);
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("Ошибка")))
            );
        }
    }

    // ===================== ШАГ 1: НАЗВАНИЕ =====================

    @Test
    @DisplayName("✅ handleTitle должен принимать корректное название")
    void handleTitle_ShouldAcceptValidTitle() {
        when(message.getText()).thenReturn("Valid Title");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_TITLE);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getTitle()).isEqualTo("Valid Title");
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CREATING_MEETING_DESC));
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleTitle должен отклонять слишком короткое название")
    void handleTitle_ShouldRejectTooShortTitle() {
        when(message.getText()).thenReturn("ab");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_TITLE);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService, never()).setState(any(), any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("короче 3")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleTitle должен отклонять слишком длинное название")
    void handleTitle_ShouldRejectTooLongTitle() {
        String longTitle = "a".repeat(51);
        when(message.getText()).thenReturn(longTitle);
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_TITLE);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService, never()).setState(any(), any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("превышать 50")))
            );
        }
    }

    // ===================== ШАГ 2: ОПИСАНИЕ =====================

    @Test
    @DisplayName("✅ handleDescription должен принимать описание")
    void handleDescription_ShouldAcceptDescription() {
        when(message.getText()).thenReturn("Test description");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_DESC);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getDescription()).isEqualTo("Test description");
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CREATING_MEETING_TAGS));
        }
    }

    @Test
    @DisplayName("✅ handleDescription должен принимать пропуск через кнопку")
    void handleDescription_ShouldAcceptSkip() {
        when(message.getText()).thenReturn("⏭️ Пропустить");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_DESC);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getDescription()).isNull();
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CREATING_MEETING_TAGS));
        }
    }

    @Test
    @DisplayName("✅ handleDescription должен отклонять слишком длинное описание")
    void handleDescription_ShouldRejectTooLongDescription() {
        String longDesc = "a".repeat(1001);
        when(message.getText()).thenReturn(longDesc);
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_DESC);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService, never()).setState(any(), any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("слишком длинное")))
            );
        }
    }

    // ===================== ШАГ 3: ТЕГИ =====================

    @Test
    @DisplayName("✅ handleTags должен показывать клавиатуру с тегами")
    void handleTags_ShouldShowTagKeyboard() {
        when(message.getText()).thenReturn("/skip");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_TAGS);

        InlineKeyboardMarkup mockInlineKeyboard = mock(InlineKeyboardMarkup.class);
        when(keyboardFactory.createTagSelectionKeyboard(anySet())).thenReturn(mockInlineKeyboard);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(CHAT_ID), anyString(), eq(mockInlineKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleTags должен предупреждать о текстовом вводе")
    void handleTags_ShouldWarnAboutTextInput() {
        when(message.getText()).thenReturn("some text");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_TAGS);

        InlineKeyboardMarkup mockInlineKeyboard = mock(InlineKeyboardMarkup.class);
        when(keyboardFactory.createTagSelectionKeyboard(anySet())).thenReturn(mockInlineKeyboard);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("нельзя вводить текст")))
            );
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(CHAT_ID), anyString(), eq(mockInlineKeyboard))
            );
        }
    }

    // ===================== ШАГ 4: ДАТА/ВРЕМЯ =====================

    @Test
    @DisplayName("✅ handleDateTime должен принимать корректную дату")
    void handleDateTime_ShouldAcceptValidDate() {
        String validDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        when(message.getText()).thenReturn(validDate);
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_DATE);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getDateTime()).isNotNull();
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CREATING_MEETING_LOC));
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleDateTime должен принимать пропуск через кнопку")
    void handleDateTime_ShouldAcceptSkip() {
        when(message.getText()).thenReturn("⏭️ Пропустить");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_DATE);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getDateTime()).isNull();
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CREATING_MEETING_LOC));
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleDateTime должен отклонять дату в прошлом")
    void handleDateTime_ShouldRejectPastDate() {
        String pastDate = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        when(message.getText()).thenReturn(pastDate);
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_DATE);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService, never()).setState(any(), any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("не может быть в прошлом")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleDateTime должен отклонять неверный формат")
    void handleDateTime_ShouldRejectInvalidFormat() {
        when(message.getText()).thenReturn("invalid date");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_DATE);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService, never()).setState(any(), any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("Не могу распознать")))
            );
        }
    }

    // ===================== ШАГ 5: МЕСТО =====================

    @Test
    @DisplayName("✅ handleLocation должен принимать место")
    void handleLocation_ShouldAcceptLocation() {
        when(message.getText()).thenReturn("Test Location");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_LOC);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getLocation()).isEqualTo("Test Location");
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CREATING_MEETING_MAX));
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleLocation должен принимать пропуск через кнопку")
    void handleLocation_ShouldAcceptSkip() {
        when(message.getText()).thenReturn("⏭️ Пропустить");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_LOC);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getLocation()).isNull();
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CREATING_MEETING_MAX));
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleLocation должен отклонять слишком длинное место")
    void handleLocation_ShouldRejectTooLongLocation() {
        String longLocation = "a".repeat(101);
        when(message.getText()).thenReturn(longLocation);
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_LOC);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService, never()).setState(any(), any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("слишком длинное")))
            );
        }
    }

    // ===================== ШАГ 6: МАКС. УЧАСТНИКОВ =====================

    @Test
    @DisplayName("✅ handleMaxPeople должен принимать число")
    void handleMaxPeople_ShouldAcceptNumber() {
        when(message.getText()).thenReturn("5");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_MAX);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getMaxPeople()).isEqualTo(5);
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CONFIRM_MEETING));
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleMaxPeople должен принимать пропуск через кнопку")
    void handleMaxPeople_ShouldAcceptSkip() {
        when(message.getText()).thenReturn("⏭️ Пропустить");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_MAX);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getMaxPeople()).isNull();
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CONFIRM_MEETING));
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleMaxPeople должен принимать пустой ввод")
    void handleMaxPeople_ShouldAcceptEmptyInput() {
        when(message.getText()).thenReturn("");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_MAX);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            assertThat(pendingMeeting.getMaxPeople()).isNull();
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), eq(pendingMeeting));
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CONFIRM_MEETING));
        }
    }

    @Test
    @DisplayName("✅ handleMaxPeople должен отклонять слишком маленькое число")
    void handleMaxPeople_ShouldRejectTooSmallNumber() {
        when(message.getText()).thenReturn("1");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_MAX);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService, never()).setState(any(), any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("хотя бы 2")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleMaxPeople должен отклонять слишком большое число")
    void handleMaxPeople_ShouldRejectTooLargeNumber() {
        when(message.getText()).thenReturn("101");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_MAX);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService, never()).setState(any(), any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("Слишком много")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleMaxPeople должен отклонять нечисловой ввод")
    void handleMaxPeople_ShouldRejectNonNumericInput() {
        when(message.getText()).thenReturn("not a number");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CREATING_MEETING_MAX);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService, never()).setState(any(), any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("Введите число")))
            );
        }
    }

    // ===================== ШАГ 7: ПОДТВЕРЖДЕНИЕ =====================

    @Test
    @DisplayName("✅ handleConfirm должен создавать встречу при подтверждении")
    void handleConfirm_ShouldCreateMeeting_WhenConfirmed() {
        when(message.getText()).thenReturn("✅ Подтвердить");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CONFIRM_MEETING);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(meetingService).createMeeting(eq(USERNAME), any());
            verify(notificationService).notifyAllUsersAboutNewMeeting(any());
            verify(stateService).resetState(CHAT_ID);
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleConfirm должен отменять при отмене")
    void handleConfirm_ShouldCancel_WhenCancelled() {
        when(message.getText()).thenReturn("❌ Отмена");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CONFIRM_MEETING);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService).resetState(CHAT_ID);
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithKeyboard(eq(CHAT_ID), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleConfirm должен игнорировать другой ввод")
    void handleConfirm_ShouldIgnoreOtherInput() {
        when(message.getText()).thenReturn("something else");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CONFIRM_MEETING);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(meetingService, never()).createMeeting(any(), any());
            verify(stateService, never()).resetState(any());
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("подтвердите или отмените")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleConfirm должен обрабатывать ошибки при создании")
    void handleConfirm_ShouldHandleErrors() {
        when(message.getText()).thenReturn("✅ Подтвердить");
        when(stateService.isCreatingMeeting(CHAT_ID)).thenReturn(true);
        when(stateService.getState(CHAT_ID)).thenReturn(UserState.CONFIRM_MEETING);

        doThrow(new RuntimeException("DB Error")).when(meetingService).createMeeting(any(), any());

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            meetingCreationHandler.handle(message);

            verify(stateService).resetState(CHAT_ID);
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("Ошибка")))
            );
        }
    }
}