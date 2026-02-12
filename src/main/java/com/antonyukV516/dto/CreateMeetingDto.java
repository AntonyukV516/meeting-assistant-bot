package com.antonyukV516.dto;

import com.antonyukV516.model.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateMeetingDto {

    @NotBlank(message = "Название встречи обязательно")
    @Size(min = 3, max = 50, message = "Название должно быть от 3 до 50 символов")
    String title;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    String description;

    List<Tag> tags;

    LocalDateTime dateTime;

    @Size(max = 100, message = "Место не должно превышать 100 символов")
    String location;

    Integer maxPeople;
}