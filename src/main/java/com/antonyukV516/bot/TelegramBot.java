package com.antonyukV516.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final UpdateHandler updateHandler;  // ✅ Только хендлер!

    public TelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            UpdateHandler updateHandler) {      // ✅ Зависимость только от хендлера
        super(botToken);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.updateHandler = updateHandler;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Received update: {}", update.getUpdateId());

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.debug("Update ignored (not a text message)");
            return;
        }
        updateHandler.handle(update);
    }
}