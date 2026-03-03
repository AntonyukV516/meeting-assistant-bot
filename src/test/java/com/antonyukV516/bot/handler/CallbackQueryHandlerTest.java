package com.antonyukV516.bot.handler;

import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.bot.state.PendingMeeting;
import com.antonyukV516.bot.state.UserState;
import com.antonyukV516.bot.state.UserStateService;
import com.antonyukV516.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CallbackQueryHandler Tests")
class CallbackQueryHandlerTest {

    @Mock
    private UserStateService stateService;

    @Mock
    private KeyboardFactory keyboardFactory;

    @InjectMocks
    private CallbackQueryHandler callbackHandler;

    @Mock
    private Update update;

    @Mock
    private CallbackQuery callbackQuery;

    @Mock
    private Message message;

    @Mock
    private User user;

    private final Long CHAT_ID = 12345L;
    private final Integer MESSAGE_ID = 678;
    private final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getMessageId()).thenReturn(MESSAGE_ID);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(user.getUserName()).thenReturn(USERNAME);
    }

    @Test
    @DisplayName("✅ canHandle должен вернуть true при наличии callback")
    void canHandle_ShouldReturnTrue_WhenUpdateHasCallback() {
        when(update.hasCallbackQuery()).thenReturn(true);

        boolean result = callbackHandler.canHandle(update);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("✅ canHandle должен вернуть false при отсутствии callback")
    void canHandle_ShouldReturnFalse_WhenUpdateHasNoCallback() {
        when(update.hasCallbackQuery()).thenReturn(false);

        boolean result = callbackHandler.canHandle(update);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("✅ handle должен вызывать handleTagSelection для tag_ callback")
    void handle_ShouldCallHandleTagSelection_ForTagCallback() {
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("tag_COFFEE");

        callbackHandler.handle(update);

        // Проверяем косвенно через вызовы сервисов
        verify(stateService, never()).setState(any(), any());
        verify(keyboardFactory, never()).createJoinButton(any());
    }

    @Test
    @DisplayName("✅ handle должен вызывать handleTagsDone для tags_done callback")
    void handle_ShouldCallHandleTagsDone_ForTagsDoneCallback() {
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("tags_done");

        callbackHandler.handle(update);

        // Проверяем, что состояние не меняется (будет меняться внутри handleTagsDone)
        verify(stateService, never()).setState(any(), any());
    }

    @Test
    @DisplayName("✅ handle должен вызывать handleJoin для join_ callback")
    void handle_ShouldCallHandleJoin_ForJoinCallback() {
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("join_123e4567-e89b-12d3-a456-426614174000");

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            callbackHandler.handle(update);

            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("записаны")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleTagSelection должен добавлять тег, если его нет")
    void handleTagSelection_ShouldAddTag_WhenNotSelected() {
        // given
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("tag_SPORT");

        PendingMeeting pending = PendingMeeting.builder()
                .chatId(CHAT_ID)
                .tags(new ArrayList<>())
                .build();
        when(stateService.getPendingMeeting(CHAT_ID)).thenReturn(pending);

        InlineKeyboardMarkup mockKeyboard = mock(InlineKeyboardMarkup.class);
        when(keyboardFactory.createTagSelectionKeyboard(anySet())).thenReturn(mockKeyboard);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            callbackHandler.handle(update);

            // then
            assertThat(pending.getTags()).contains(Tag.SPORT);
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), any());
            telegramBotMock.verify(() ->
                    TelegramBot.editMessageKeyboard(eq(CHAT_ID), eq(MESSAGE_ID), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleTagSelection должен удалять тег, если он уже выбран")
    void handleTagSelection_ShouldRemoveTag_WhenAlreadySelected() {
        // given
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("tag_SPORT");

        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.SPORT);
        PendingMeeting pending = PendingMeeting.builder()
                .chatId(CHAT_ID)
                .tags(tags)
                .build();
        when(stateService.getPendingMeeting(CHAT_ID)).thenReturn(pending);

        InlineKeyboardMarkup mockKeyboard = mock(InlineKeyboardMarkup.class);
        when(keyboardFactory.createTagSelectionKeyboard(anySet())).thenReturn(mockKeyboard);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            callbackHandler.handle(update);

            // then
            assertThat(pending.getTags()).doesNotContain(Tag.SPORT);
            verify(stateService).updatePendingMeeting(eq(CHAT_ID), any());
            telegramBotMock.verify(() ->
                    TelegramBot.editMessageKeyboard(eq(CHAT_ID), eq(MESSAGE_ID), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ handleTagSelection должен обрабатывать неверный тег без ошибок")
    void handleTagSelection_ShouldHandleInvalidTag_WithoutErrors() {
        // given
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("tag_INVALID");

        PendingMeeting pending = PendingMeeting.builder()
                .chatId(CHAT_ID)
                .tags(new ArrayList<>())
                .build();
        when(stateService.getPendingMeeting(CHAT_ID)).thenReturn(pending);

        // when
        callbackHandler.handle(update);

        // then - просто не должно быть исключений
        verify(stateService, never()).updatePendingMeeting(any(), any());
    }

    @Test
    @DisplayName("✅ handleTagSelection должен отправлять ошибку, если нет PendingMeeting")
    void handleTagSelection_ShouldSendError_WhenNoPendingMeeting() {
        // given
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("tag_COFFEE");
        when(stateService.getPendingMeeting(CHAT_ID)).thenReturn(null);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            callbackHandler.handle(update);

            // then
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("истекла")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleTagsDone должен переходить к следующему шагу")
    void handleTagsDone_ShouldMoveToNextStep() {
        // given
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("tags_done");

        PendingMeeting pending = PendingMeeting.builder()
                .chatId(CHAT_ID)
                .tags(List.of(Tag.COFFEE, Tag.WORK))
                .build();
        when(stateService.getPendingMeeting(CHAT_ID)).thenReturn(pending);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            callbackHandler.handle(update);

            // then
            verify(stateService).setState(eq(CHAT_ID), eq(UserState.CREATING_MEETING_DATE));
            telegramBotMock.verify(() ->
                    TelegramBot.deleteMessage(eq(CHAT_ID), eq(MESSAGE_ID))
            );
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("✅ Теги сохранены")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleTagsDone должен отправлять ошибку, если нет PendingMeeting")
    void handleTagsDone_ShouldSendError_WhenNoPendingMeeting() {
        // given
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("tags_done");
        when(stateService.getPendingMeeting(CHAT_ID)).thenReturn(null);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            callbackHandler.handle(update);

            // then
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("истекла")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleJoin должен обрабатывать корректный UUID")
    void handleJoin_ShouldProcessValidUUID() {
        // given
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("join_123e4567-e89b-12d3-a456-426614174000");

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            callbackHandler.handle(update);

            // then
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("записаны")))
            );
        }
    }

    @Test
    @DisplayName("✅ handleJoin должен обрабатывать некорректный UUID")
    void handleJoin_ShouldHandleInvalidUUID() {
        // given
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("join_invalid-uuid");

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            callbackHandler.handle(update);

            // then
            telegramBotMock.verify(() ->
                    TelegramBot.send(eq(CHAT_ID), argThat(text -> text.contains("некорректный")))
            );
        }
    }
}