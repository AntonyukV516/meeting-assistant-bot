package com.antonyukV516.bot.state;

public enum UserState {
    NONE,                    // Обычное состояние
    CREATING_MEETING_TITLE,  // Шаг 1: название
    CREATING_MEETING_DESC,   // Шаг 2: описание
    CREATING_MEETING_TAGS,   // Шаг 3: теги
    CREATING_MEETING_DATE,   // Шаг 4: дата/время
    CREATING_MEETING_LOC,    // Шаг 5: место
    CREATING_MEETING_MAX,    // Шаг 6: макс. людей
    CONFIRM_MEETING          // Шаг 7: подтверждение
}
