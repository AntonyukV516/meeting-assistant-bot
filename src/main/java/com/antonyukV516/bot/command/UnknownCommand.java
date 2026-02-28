package com.antonyukV516.bot.command;

import com.antonyukV516.bot.CommandHandler;
import com.antonyukV516.bot.TelegramBot;
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
                🤔 Я не понимаю такую команду
                                
                Доступные команды:
                /start - начать работу с ботом
                /new создать новую встречу
                """;

        TelegramBot.send(chatId, response);
    }
}