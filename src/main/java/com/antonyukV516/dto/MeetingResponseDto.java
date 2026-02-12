package com.antonyukV516.dto;

import com.antonyukV516.model.Tag;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MeetingResponseDto {
    UUID id;
    String title;
    String description;
    List<Tag> tags;
    LocalDateTime dateTime;
    String location;
    Integer maxPeople;
    String creatorUsername;
}