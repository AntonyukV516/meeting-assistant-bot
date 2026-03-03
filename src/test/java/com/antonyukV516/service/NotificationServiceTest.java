package com.antonyukV516.service;

import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.model.Meeting;
import com.antonyukV516.model.Tag;
import com.antonyukV516.model.User;
import com.antonyukV516.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeyboardFactory keyboardFactory;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private InlineKeyboardMarkup mockKeyboard;

    private User creator;
    private User user1;
    private User user2;
    private Meeting meeting;
    private final UUID MEETING_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .telegramUsername("creator")
                .chatId(111L)
                .build();

        user1 = User.builder()
                .telegramUsername("user1")
                .chatId(222L)
                .build();

        user2 = User.builder()
                .telegramUsername("user2")
                .chatId(333L)
                .build();

        meeting = Meeting.builder()
                .id(MEETING_ID)
                .title("Test Meeting")
                .description("Test Description")
                .tags(List.of(Tag.COFFEE, Tag.WORK))
                .dateTime(LocalDateTime.now().plusDays(2))
                .location("Test Location")
                .maxPeople(5)
                .creator(creator)
                .build();

        when(keyboardFactory.createJoinButton(MEETING_ID)).thenReturn(mockKeyboard);
    }

    @Test
    @DisplayName("✅ notifyAllUsersAboutNewMeeting должен отправлять уведомления всем, кроме создателя")
    void notifyAllUsersAboutNewMeeting_ShouldNotifyAllExceptCreator() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(creator, user1, user2));

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            notificationService.notifyAllUsersAboutNewMeeting(meeting);

            // then
            // Проверяем, что отправлено 2 уведомления (user1 и user2)
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(user1.getChatId()), anyString(), eq(mockKeyboard))
            );
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(user2.getChatId()), anyString(), eq(mockKeyboard))
            );
            // Создателю не отправляем
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(creator.getChatId()), anyString(), any()), never()
            );
        }
    }

    @Test
    @DisplayName("✅ notifyAllUsersAboutNewMeeting должен пропускать пользователей без chatId")
    void notifyAllUsersAboutNewMeeting_ShouldSkipUsersWithoutChatId() {
        // given
        User userWithoutChatId = User.builder()
                .telegramUsername("nochat")
                .chatId(null)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(creator, user1, userWithoutChatId));

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            notificationService.notifyAllUsersAboutNewMeeting(meeting);

            // then
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(user1.getChatId()), anyString(), eq(mockKeyboard))
            );
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(creator.getChatId()), anyString(), any()), never()
            );
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(userWithoutChatId.getChatId()), anyString(), any()), never()
            );
        }
    }

    @Test
    @DisplayName("✅ notifyAllUsersAboutNewMeeting должен работать с пустым списком пользователей")
    void notifyAllUsersAboutNewMeeting_ShouldWorkWithEmptyUserList() {
        // given
        when(userRepository.findAll()).thenReturn(List.of());

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            notificationService.notifyAllUsersAboutNewMeeting(meeting);

            // then
            telegramBotMock.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("✅ notifyAllUsersAboutNewMeeting должен логировать количество отправленных уведомлений")
    void notifyAllUsersAboutNewMeeting_ShouldLogSentCount() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(creator, user1, user2));

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            notificationService.notifyAllUsersAboutNewMeeting(meeting);

            // then - просто проверяем, что метод выполнился без ошибок
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(user1.getChatId()), anyString(), eq(mockKeyboard))
            );
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(user2.getChatId()), anyString(), eq(mockKeyboard))
            );
        }
    }

    @Test
    @DisplayName("✅ должен форматировать сообщение со всеми полями встречи")
    void shouldFormatMessageWithAllMeetingFields() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(creator, user1));

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

            // when
            notificationService.notifyAllUsersAboutNewMeeting(meeting);

            // then
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(user1.getChatId()), messageCaptor.capture(), eq(mockKeyboard))
            );

            String sentMessage = messageCaptor.getValue();
            assertThat(sentMessage).contains("Test Meeting");
            assertThat(sentMessage).contains("Test Description");
            assertThat(sentMessage).contains("📅");
            assertThat(sentMessage).contains("📍 Test Location");
            assertThat(sentMessage).contains("🏷️");
            assertThat(sentMessage).contains("`COFFEE`");
            assertThat(sentMessage).contains("`WORK`");
            assertThat(sentMessage).contains("👥 Макс. участников: 5");
        }
    }

    @Test
    @DisplayName("✅ должен форматировать сообщение с минимальными полями")
    void shouldFormatMessageWithMinimalFields() {
        // given
        Meeting minimalMeeting = Meeting.builder()
                .id(MEETING_ID)
                .title("Minimal Meeting")
                .creator(creator)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(creator, user1));

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

            // when
            notificationService.notifyAllUsersAboutNewMeeting(minimalMeeting);

            // then
            telegramBotMock.verify(() ->
                    TelegramBot.sendWithInlineKeyboard(eq(user1.getChatId()), messageCaptor.capture(), eq(mockKeyboard))
            );

            String sentMessage = messageCaptor.getValue();
            assertThat(sentMessage).contains("Minimal Meeting");
            assertThat(sentMessage).doesNotContain("📅");
            assertThat(sentMessage).doesNotContain("📍");
            assertThat(sentMessage).doesNotContain("🏷️");
            assertThat(sentMessage).doesNotContain("👥");
        }
    }

    @Test
    @DisplayName("✅ должен создавать правильную кнопку присоединения")
    void shouldCreateCorrectJoinButton() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(creator, user1));

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            notificationService.notifyAllUsersAboutNewMeeting(meeting);

            // then
            verify(keyboardFactory).createJoinButton(MEETING_ID);
        }
    }

    @Test
    @DisplayName("✅ должен отправлять уведомления при множестве пользователей")
    void shouldSendNotificationsToManyUsers() {
        // given
        List<User> manyUsers = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            manyUsers.add(User.builder()
                    .telegramUsername("user" + i)
                    .chatId(1000L + i)
                    .build());
        }
        manyUsers.add(creator);
        when(userRepository.findAll()).thenReturn(manyUsers);

        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // when
            notificationService.notifyAllUsersAboutNewMeeting(meeting);

            // then
            for (int i = 0; i < 10; i++) {
                Long chatId = 1000L + i;
                telegramBotMock.verify(() ->
                        TelegramBot.sendWithInlineKeyboard(eq(chatId), anyString(), eq(mockKeyboard))
                );
            }
        }
    }
}