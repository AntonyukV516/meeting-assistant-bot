package com.antonyukV516.bot.state;

import com.antonyukV516.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Временные данные создаваемой встречи.
 * <p>
 * Хранит данные, которые пользователь ввел в процессе диалога создания встречи.
 * Живет только в памяти (в {@link UserStateService}) и удаляется после создания встречи
 * или отмены диалога.
 * </p>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see UserStateService
 * @see com.antonyukV516.bot.handler.MeetingCreationHandler
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingMeeting {
    /** Username создателя встречи */
    private String creatorUsername;

    /** ID чата для отправки сообщений */
    private Long chatId;

    /** Название встречи (обязательное поле) */
    private String title;

    /** Описание встречи (опционально) */
    private String description;

    /** Выбранные теги */
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    /** Дата и время встречи (опционально) */
    private LocalDateTime dateTime;

    /** Место проведения (опционально) */
    private String location;

    /** Максимальное количество участников (опционально) */
    private Integer maxPeople;
}