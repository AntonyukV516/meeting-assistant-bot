package com.antonyukV516.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private static TelegramBot instance;

    private final String botToken;
    private final String botUsername;
    private final UpdateHandler updateHandler;

    public TelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            UpdateHandler updateHandler) {
        super(botToken);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.updateHandler = updateHandler;
        TelegramBot.instance = this;
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

    public static void send(Long chatId, String text) {
        if (instance == null) {
            log.error("TelegramBot instance not initialized");
            return;
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .build();

        try {
            instance.execute(message);
            log.debug("Message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId: {}", chatId, e);
        }
    }

    public static void sendWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        if (instance == null) {
            log.error("TelegramBot instance not initialized");
            return;
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            instance.execute(message);
            log.debug("Message with keyboard sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId: {}", chatId, e);
        }
    }
}