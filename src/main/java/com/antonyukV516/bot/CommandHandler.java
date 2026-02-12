package com.antonyukV516.bot;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface CommandHandler {
    boolean canHandle(String text);
    void handle(Message message);
}