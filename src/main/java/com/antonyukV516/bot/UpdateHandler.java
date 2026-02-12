package com.antonyukV516.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateHandler {

    private final CommandDispatcher commandDispatcher;

    public void handle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.debug("Update ignored (not a text message)");
            return;
        }

        var message = update.getMessage();
        var from = message.getFrom();
        String username = from != null ? from.getUserName() : "unknown";

        log.info("Message from @{} (chatId: {}): {}",
                username, message.getChatId(), message.getText());

        commandDispatcher.dispatch(message);
    }
}