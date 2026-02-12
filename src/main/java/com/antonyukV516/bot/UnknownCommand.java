package com.antonyukV516.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnknownCommand implements CommandHandler {

    @Override
    public boolean canHandle(String text) {
        return true;
    }

    @Override
    public void handle(Message message) {
        Long chatId = message.getChatId();

        String response = """
                🤔 Я пока понимаю только команду /start
                                
                Доступные команды:
                /start - начать работу с ботом
                """;

        TelegramBot.send(chatId, response);
    }
}