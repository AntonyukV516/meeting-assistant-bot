package com.antonyukV516.bot.state;

/**
        * Состояния пользователя в диалоге создания встречи.
        * <p>
 * Используется {@link UserStateService} для отслеживания текущего шага пользователя.
 * Переход между состояниями происходит последовательно от {@link #CREATING_MEETING_TITLE}
        * до {@link #CONFIRM_MEETING}.
        * </p>
        *
        * @author AntonyukV516
 * @version 1.0
        * @see UserStateService
 * @see PendingMeeting
 */
public enum UserState {

    /** Обычное состояние (пользователь не в диалоге) */
    NONE,

    /** Ожидание ввода названия встречи (шаг 1) */
    CREATING_MEETING_TITLE,

    /** Ожидание ввода описания (шаг 2, опционально) */
    CREATING_MEETING_DESC,

    /** Ожидание выбора тегов (шаг 3, инлайн-кнопки) */
    CREATING_MEETING_TAGS,

    /** Ожидание ввода даты и времени (шаг 4) */
    CREATING_MEETING_DATE,

    /** Ожидание ввода места (шаг 5, опционально) */
    CREATING_MEETING_LOC,

    /** Ожидание ввода максимального количества участников (шаг 6, опционально) */
    CREATING_MEETING_MAX,

    /** Ожидание подтверждения создания встречи (шаг 7) */
    CONFIRM_MEETING
}
