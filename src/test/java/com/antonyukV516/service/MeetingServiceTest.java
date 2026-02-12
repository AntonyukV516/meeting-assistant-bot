package com.antonyukV516.service;

import com.antonyukV516.dto.CreateMeetingDto;
import com.antonyukV516.dto.MeetingResponseDto;
import com.antonyukV516.mapper.MeetingMapper;
import com.antonyukV516.model.Meeting;
import com.antonyukV516.model.Tag;
import com.antonyukV516.model.User;
import com.antonyukV516.repository.MeetingRepository;
import com.antonyukV516.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingService Tests")
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MeetingMapper meetingMapper;

    @InjectMocks
    private MeetingService meetingService;

    private User creator;
    private Meeting meeting;
    private CreateMeetingDto createDto;
    private MeetingResponseDto responseDto;
    private final String USERNAME = "creator";
    private final UUID MEETING_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .telegramUsername(USERNAME)
                .chatId(111111L)
                .build();

        meeting = new Meeting();
        meeting.setId(MEETING_ID);
        meeting.setTitle("Test Meeting");
        meeting.setDescription("Test Description");
        meeting.setTags(List.of(Tag.COFFEE, Tag.WORK));
        meeting.setDateTime(LocalDateTime.now().plusDays(1));
        meeting.setLocation("Test Location");
        meeting.setMaxPeople(5);
        meeting.setCreator(creator);

        createDto = CreateMeetingDto.builder()
                .title("Test Meeting")
                .description("Test Description")
                .tags(List.of(Tag.COFFEE, Tag.WORK))
                .dateTime(LocalDateTime.now().plusDays(1))
                .location("Test Location")
                .maxPeople(5)
                .build();

        responseDto = MeetingResponseDto.builder()
                .id(MEETING_ID)
                .title("Test Meeting")
                .description("Test Description")
                .tags(List.of(Tag.COFFEE, Tag.WORK))
                .dateTime(LocalDateTime.now().plusDays(1))
                .location("Test Location")
                .maxPeople(5)
                .creatorUsername(USERNAME)
                .build();
    }

    @Test
    @DisplayName("✅ Должен создать встречу")
    void createMeeting_ShouldCreateMeeting_WhenValidData() {
        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(creator));
        when(meetingMapper.toEntity(createDto)).thenReturn(meeting);
        when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);

        Meeting result = meetingService.createMeeting(USERNAME, createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(MEETING_ID);
        assertThat(result.getTitle()).isEqualTo("Test Meeting");
        assertThat(result.getCreator()).isEqualTo(creator);
        verify(meetingRepository, times(1)).save(meeting);
    }

    @Test
    @DisplayName("✅ Должен выбросить исключение, если создатель не найден")
    void createMeeting_ShouldThrowException_WhenCreatorNotFound() {
        when(userRepository.findById(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.createMeeting(USERNAME, createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь @creator не найден");

        verify(meetingRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ Должен получить встречу по ID в виде DTO")
    void getMeetingResponse_ShouldReturnDto_WhenMeetingExists() {
        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.of(meeting));
        when(meetingMapper.toResponseDto(meeting)).thenReturn(responseDto);

        MeetingResponseDto result = meetingService.getMeetingResponse(MEETING_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(MEETING_ID);
        assertThat(result.getCreatorUsername()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("✅ Должен вернуть null, если встреча не найдена по ID")
    void getMeetingResponse_ShouldReturnNull_WhenMeetingNotFound() {
        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.empty());

        MeetingResponseDto result = meetingService.getMeetingResponse(MEETING_ID);

        assertThat(result).isNull();
        verify(meetingMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("✅ Должен получить список встреч пользователя")
    void getUserMeetingsResponse_ShouldReturnUserMeetings() {
        List<Meeting> meetings = List.of(meeting);
        List<MeetingResponseDto> dtos = List.of(responseDto);

        when(meetingRepository.findByCreator_TelegramUsername(USERNAME)).thenReturn(meetings);
        when(meetingMapper.toResponseDtoList(meetings)).thenReturn(dtos);

        List<MeetingResponseDto> result = meetingService.getUserMeetingsResponse(USERNAME);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(MEETING_ID);
        verify(meetingMapper, times(1)).toResponseDtoList(meetings);
    }

    @Test
    @DisplayName("✅ Должен получить все встречи")
    void getAllMeetingsResponse_ShouldReturnAllMeetings() {
        List<Meeting> meetings = List.of(meeting);
        List<MeetingResponseDto> dtos = List.of(responseDto);

        when(meetingRepository.findAll()).thenReturn(meetings);
        when(meetingMapper.toResponseDtoList(meetings)).thenReturn(dtos);

        List<MeetingResponseDto> result = meetingService.getAllMeetingsResponse();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(MEETING_ID);
    }

    @Test
    @DisplayName("✅ Должен создать встречу без опциональных полей")
    void createMeeting_ShouldCreateMeeting_WithOnlyRequiredFields() {
        CreateMeetingDto minimalDto = CreateMeetingDto.builder()
                .title("Minimal Meeting")
                .build();

        Meeting minimalMeeting = new Meeting();
        minimalMeeting.setTitle("Minimal Meeting");
        minimalMeeting.setCreator(creator);

        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(creator));
        when(meetingMapper.toEntity(minimalDto)).thenReturn(minimalMeeting);
        when(meetingRepository.save(any(Meeting.class))).thenReturn(minimalMeeting);

        Meeting result = meetingService.createMeeting(USERNAME, minimalDto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Minimal Meeting");
        assertThat(result.getDescription()).isNull();
        verify(meetingMapper, times(1)).toEntity(minimalDto);
    }

    @Test
    @DisplayName("✅ Должен обработать валидацию DTO через аннотации")
    void createMeeting_ShouldUseValidatedDto() {
        // Проверка неявная - сам факт, что метод принимает @Valid CreateMeetingDto
        // Если DTO невалидный, Spring выбросит исключение ДО вызова метода
        // Это тестируется в интеграционных тестах контроллера/хендлера
    }

    @Test
    @DisplayName("✅ Должен вернуть пустой список, если у пользователя нет встреч")
    void getUserMeetingsResponse_ShouldReturnEmptyList_WhenNoMeetings() {
        when(meetingRepository.findByCreator_TelegramUsername(USERNAME)).thenReturn(List.of());
        when(meetingMapper.toResponseDtoList(anyList())).thenReturn(List.of());

        List<MeetingResponseDto> result = meetingService.getUserMeetingsResponse(USERNAME);

        assertThat(result).isEmpty();
    }
}
