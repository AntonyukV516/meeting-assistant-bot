package com.antonyukV516.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
public class CommandDispatcher {

    private final List<CommandHandler> commandHandlers;

    public CommandDispatcher(List<CommandHandler> commandHandlers) {
        this.commandHandlers = commandHandlers;
    }

    public void dispatch(Message message) {
        String text = message.getText().trim();
        Long chatId = message.getChatId();

        for (CommandHandler handler : commandHandlers) {
            if (handler.canHandle(text, chatId)) {
                handler.handle(message);
                return;
            }
        }
    }
}