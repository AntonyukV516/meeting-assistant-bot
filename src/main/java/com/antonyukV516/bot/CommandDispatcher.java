package com.antonyukV516.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

/**
 * Диспетчер команд.
 * <p>
 * Получает сообщение и ищет подходящий {@link CommandHandler} для его обработки.
 * Перебирает все зарегистрированные обработчики в порядке их добавления.
 * </p>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see CommandHandler
 */
@Component
public class CommandDispatcher {

    private final List<CommandHandler> commandHandlers;

    /**
     * Создает диспетчер со списком всех обработчиков.
     *
     * @param commandHandlers список всех команд (порядок важен!)
     */
    public CommandDispatcher(List<CommandHandler> commandHandlers) {
        this.commandHandlers = commandHandlers;
    }

    /**
     * Находит подходящий обработчик и передает ему сообщение.
     *
     * @param message сообщение от пользователя
     */
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