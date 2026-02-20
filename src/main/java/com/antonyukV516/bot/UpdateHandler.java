package com.antonyukV516.bot;

import com.antonyukV516.bot.handler.CallbackQueryHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateHandler {

    private final CommandDispatcher commandDispatcher;
    private final CallbackQueryHandler callbackHandler;

    public void handle(Update update) {
        if (callbackHandler.canHandle(update)) {
            callbackHandler.handle(update);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            commandDispatcher.dispatch(update.getMessage());
        } else {
            log.debug("Update ignored (not a text message)");
        }
    }
}