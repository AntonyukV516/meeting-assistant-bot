package com.antonyukV516.bot;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface CommandHandler {

    void handle(Message message);

    default boolean canHandle(String text) {
        return false;
    }

    default boolean canHandle(String text, Long chatId) {
        return canHandle(text);
    }
}