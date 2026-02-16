package com.antonyukV516.bot.state;

import com.antonyukV516.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingMeeting {
    private String creatorUsername;
    private Long chatId;

    private String title;
    private String description;

    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    private LocalDateTime dateTime;
    private String location;
    private Integer maxPeople;

    private int step;
}
