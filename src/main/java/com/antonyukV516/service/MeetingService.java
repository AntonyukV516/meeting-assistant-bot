package com.antonyukV516.service;

import com.antonyukV516.dto.CreateMeetingDto;
import com.antonyukV516.dto.MeetingResponseDto;
import com.antonyukV516.mapper.MeetingMapper;
import com.antonyukV516.model.Meeting;
import com.antonyukV516.model.User;
import com.antonyukV516.repository.MeetingRepository;
import com.antonyukV516.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingMapper meetingMapper;

    @Transactional
    public Meeting createMeeting(String creatorUsername, @Valid CreateMeetingDto dto) {
        User creator = userRepository.findById(creatorUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Пользователь @%s не найден", creatorUsername)
                ));

        Meeting meeting = meetingMapper.toEntity(dto);
        meeting.setCreator(creator);

        Meeting savedMeeting = meetingRepository.save(meeting);
        log.info("Created meeting: {} by @{}", savedMeeting.getId(), creatorUsername);

        return savedMeeting;
    }

    public List<MeetingResponseDto> getUserMeetingsResponse(String username) {
        return meetingMapper.toResponseDtoList(
                meetingRepository.findByCreator_TelegramUsername(username)
        );
    }

    public List<MeetingResponseDto> getAllMeetingsResponse() {
        return meetingMapper.toResponseDtoList(
                meetingRepository.findAll()
        );
    }

    public MeetingResponseDto getMeetingResponse(UUID id) {
        return meetingRepository.findById(id)
                .map(meetingMapper::toResponseDto)
                .orElse(null);
    }
}