package com.antonyukV516.repository;

import com.antonyukV516.model.Meeting;
import com.antonyukV516.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Meeting Repository Tests")
class MeetingRepositoryTest {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    private final String creatorUsername = "creator";

    @BeforeEach
    void setUp() {
        User creator = new User();
        creator.setTelegramUsername(creatorUsername);
        creator.setChatId(111111L);
        userRepository.save(creator);
    }

    @Test
    @DisplayName("✅ Создать новую встречу")
    void save_ShouldCreateNewMeeting() {
        User creator = userRepository.findById(creatorUsername).orElseThrow();

        Meeting newMeeting = new Meeting();
        newMeeting.setTitle("New Meeting");
        newMeeting.setCreator(creator);

        Meeting saved = meetingRepository.save(newMeeting);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("New Meeting");

        List<Meeting> meetings = meetingRepository.findByCreator_TelegramUsername(creatorUsername);
        assertThat(meetings).hasSize(1);  // Будет 1, а не 2!
    }

    @Test
    @DisplayName("✅ Найти все встречи пользователя")
    void findByCreatorUsername_ShouldReturnUserMeetings() {
        User creator = userRepository.findById(creatorUsername).orElseThrow();

        Meeting meeting = new Meeting();
        meeting.setTitle("Test Meeting");
        meeting.setCreator(creator);
        meetingRepository.save(meeting);

        List<Meeting> meetings = meetingRepository.findByCreator_TelegramUsername(creatorUsername);
        assertThat(meetings).hasSize(1);
        assertThat(meetings.getFirst().getTitle()).isEqualTo("Test Meeting");
    }

    @Test
    @DisplayName("✅ Найти встречу по ID")
    void findById_ShouldReturnMeeting() {
        User creator = userRepository.findById(creatorUsername).orElseThrow();

        Meeting meeting = new Meeting();
        meeting.setTitle("Test Meeting");
        meeting.setCreator(creator);
        Meeting saved = meetingRepository.save(meeting);

        Optional<Meeting> found = meetingRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Meeting");
    }

    @Test
    @DisplayName("✅ Вернуть пустой список если у пользователя нет встреч")
    void findByCreatorUsername_ShouldReturnEmptyList_WhenNoMeetings() {
        List<Meeting> meetings = meetingRepository.findByCreator_TelegramUsername(creatorUsername);
        assertThat(meetings).isEmpty();
    }

    @Test
    @DisplayName("✅ Обновить встречу")
    void update_ShouldModifyMeeting() {
        User creator = userRepository.findById(creatorUsername).orElseThrow();

        Meeting meeting = new Meeting();
        meeting.setTitle("Original Title");
        meeting.setCreator(creator);
        Meeting saved = meetingRepository.save(meeting);

        saved.setTitle("Updated Title");
        Meeting updated = meetingRepository.save(saved);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("✅ Удалить встречу")
    void deleteById_ShouldRemoveMeeting() {
        User creator = userRepository.findById(creatorUsername).orElseThrow();

        Meeting meeting = new Meeting();
        meeting.setTitle("To Delete");
        meeting.setCreator(creator);
        Meeting saved = meetingRepository.save(meeting);

        meetingRepository.deleteById(saved.getId());

        Optional<Meeting> deleted = meetingRepository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("✅ Создать встречу только с обязательными полями")
    void save_ShouldWorkWithMinimalFields() {
        User creator = userRepository.findById(creatorUsername).orElseThrow();

        Meeting minimal = new Meeting();
        minimal.setTitle("Minimal");
        minimal.setCreator(creator);

        Meeting saved = meetingRepository.save(minimal);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Minimal");
        assertThat(saved.getDescription()).isNull();
    }

    @Test
    @DisplayName("✅ Найти встречи разных пользователей")
    void findByCreatorUsername_ShouldSeparateUsers() {
        User creator1 = userRepository.findById(creatorUsername).orElseThrow();
        Meeting meeting1 = new Meeting();
        meeting1.setTitle("Creator Meeting");
        meeting1.setCreator(creator1);
        meetingRepository.save(meeting1);

        User creator2 = new User();
        creator2.setTelegramUsername("other");
        creator2.setChatId(222222L);
        userRepository.save(creator2);

        creator2 = userRepository.findById("other").orElseThrow();

        Meeting meeting2 = new Meeting();
        meeting2.setTitle("Other Meeting");
        meeting2.setCreator(creator2);
        meetingRepository.save(meeting2);

        List<Meeting> creatorMeetings = meetingRepository.findByCreator_TelegramUsername("creator");
        List<Meeting> otherMeetings = meetingRepository.findByCreator_TelegramUsername("other");

        assertThat(creatorMeetings).hasSize(1);
        assertThat(creatorMeetings.getFirst().getTitle()).isEqualTo("Creator Meeting");
        assertThat(otherMeetings).hasSize(1);
        assertThat(otherMeetings.getFirst().getTitle()).isEqualTo("Other Meeting");
    }
}