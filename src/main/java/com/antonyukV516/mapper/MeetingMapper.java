package com.antonyukV516.mapper;

import com.antonyukV516.dto.CreateMeetingDto;
import com.antonyukV516.dto.MeetingResponseDto;
import com.antonyukV516.model.Meeting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MeetingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creator", ignore = true)
    Meeting toEntity(CreateMeetingDto dto);

    @Mapping(source = "creator.telegramUsername", target = "creatorUsername")
    MeetingResponseDto toResponseDto(Meeting meeting);

    List<MeetingResponseDto> toResponseDtoList(List<Meeting> meetings);
}