package com.antonyukV516.bot.state;

import com.antonyukV516.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserStateService Tests")
class UserStateServiceTest {

    private UserStateService stateService;
    private final Long CHAT_ID = 12345L;

    @BeforeEach
    void setUp() {
        stateService = new UserStateService();
    }

    @Test
    @DisplayName("✅ getState должен возвращать NONE для нового пользователя")
    void getState_ShouldReturnNone_ForNewUser() {
        UserState state = stateService.getState(CHAT_ID);
        assertThat(state).isEqualTo(UserState.NONE);
    }

    @Test
    @DisplayName("✅ setState должен сохранять состояние")
    void setState_ShouldSaveState() {
        stateService.setState(CHAT_ID, UserState.CREATING_MEETING_TITLE);

        UserState state = stateService.getState(CHAT_ID);
        assertThat(state).isEqualTo(UserState.CREATING_MEETING_TITLE);
    }

    @Test
    @DisplayName("✅ resetState должен удалять состояние и данные")
    void resetState_ShouldRemoveStateAndData() {
        // given
        stateService.setState(CHAT_ID, UserState.CREATING_MEETING_TITLE);
        PendingMeeting pending = PendingMeeting.builder().title("Test").build();
        stateService.setPendingMeeting(CHAT_ID, pending);

        // when
        stateService.resetState(CHAT_ID);

        // then
        assertThat(stateService.getState(CHAT_ID)).isEqualTo(UserState.NONE);
        assertThat(stateService.getPendingMeeting(CHAT_ID)).isNull();
    }

    @Test
    @DisplayName("✅ getPendingMeeting должен возвращать null если нет данных")
    void getPendingMeeting_ShouldReturnNull_WhenNoData() {
        PendingMeeting result = stateService.getPendingMeeting(CHAT_ID);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("✅ setPendingMeeting должен сохранять данные")
    void setPendingMeeting_ShouldSaveData() {
        PendingMeeting pending = PendingMeeting.builder()
                .chatId(CHAT_ID)
                .title("Test Meeting")
                .description("Test Description")
                .tags(List.of(Tag.COFFEE))
                .dateTime(LocalDateTime.now())
                .location("Test Location")
                .maxPeople(5)
                .build();

        stateService.setPendingMeeting(CHAT_ID, pending);

        PendingMeeting result = stateService.getPendingMeeting(CHAT_ID);
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Meeting");
        assertThat(result.getTags()).contains(Tag.COFFEE);
    }

    @Test
    @DisplayName("✅ updatePendingMeeting должен обновлять данные")
    void updatePendingMeeting_ShouldUpdateData() {
        PendingMeeting pending = PendingMeeting.builder()
                .chatId(CHAT_ID)
                .title("Original")
                .build();
        stateService.setPendingMeeting(CHAT_ID, pending);

        pending.setTitle("Updated");
        stateService.updatePendingMeeting(CHAT_ID, pending);

        PendingMeeting result = stateService.getPendingMeeting(CHAT_ID);
        assertThat(result.getTitle()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("✅ isCreatingMeeting должен возвращать true если состояние не NONE")
    void isCreatingMeeting_ShouldReturnTrue_WhenStateNotNone() {
        stateService.setState(CHAT_ID, UserState.CREATING_MEETING_TITLE);

        boolean result = stateService.isCreatingMeeting(CHAT_ID);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("✅ isCreatingMeeting должен возвращать false если состояние NONE")
    void isCreatingMeeting_ShouldReturnFalse_WhenStateNone() {
        boolean result = stateService.isCreatingMeeting(CHAT_ID);
        assertThat(result).isFalse();
    }
}