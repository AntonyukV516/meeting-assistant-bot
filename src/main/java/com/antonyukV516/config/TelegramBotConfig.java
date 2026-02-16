package com.antonyukV516.config;

import com.antonyukV516.bot.*;
import com.antonyukV516.bot.command.UnknownCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramBot);
        return botsApi;
    }

    @Bean
    public CommandDispatcher commandDispatcher(
            List<CommandHandler> commandHandlers,
            UnknownCommand unknownCommand) {

        List<CommandHandler> sorted = new ArrayList<>(commandHandlers);
        sorted.remove(unknownCommand);
        sorted.add(unknownCommand);

        return new CommandDispatcher(sorted);
    }
}
