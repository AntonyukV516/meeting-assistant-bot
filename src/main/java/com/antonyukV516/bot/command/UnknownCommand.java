package com.antonyukV516.bot.command;

import com.antonyukV516.bot.CommandHandler;
import com.antonyukV516.bot.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Обработчик неизвестных команд и сообщений.
 * <p>
 * Является fallback-обработчиком — срабатывает, если ни одна другая команда
 * не смогла обработать сообщение. Всегда возвращает {@code true} в {@link #canHandle(String)}.
 * Должен быть последним в списке команд в {@link com.antonyukV516.bot.CommandDispatcher}.
 * </p>
 *
 * <p>Отправляет пользователю сообщение со списком доступных команд.</p>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see com.antonyukV516.bot.CommandHandler
 * @see com.antonyukV516.bot.CommandDispatcher
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnknownCommand implements CommandHandler {

    /**
     * {@inheritDoc}
     * <p>
     * Всегда возвращает {@code true}, так как это fallback-обработчик.
     * </p>
     *
     * @param text текст сообщения (игнорируется)
     * @return всегда {@code true}
     */
    @Override
    public boolean canHandle(String text) {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Отправляет пользователю сообщение со списком доступных команд:
     * <ul>
     *   <li>{@code /start} - начало работы</li>
     *   <li>{@code /new} - создание новой встречи</li>
     * </ul>
     * </p>
     *
     * @param message сообщение от пользователя
     */
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