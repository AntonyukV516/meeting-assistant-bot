package com.antonyukV516.bot;

import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Интерфейс для всех команд бота.
 * <p>
 * Каждая команда должна реализовать этот интерфейс и определить:
 * <ul>
 *   <li>Может ли она обработать данное сообщение ({@link #canHandle})</li>
 *   <li>Как именно обработать ({@link #handle})</li>
 * </ul>
 * </p>
 *
 * <p>Интерфейс предоставляет два метода {@code canHandle} с разными сигнатурами
 * для обратной совместимости. По умолчанию версия с {@code chatId} вызывает версию без {@code chatId}.</p>
 *
 * <p>Пример реализации для обычной команды:</p>
 * <pre>
 * {@code
 * @Component
 * public class StartCommand implements CommandHandler {
 *     @Override
 *     public boolean canHandle(String text) {
 *         return "/start".equals(text);
 *     }
 *
 *     @Override
 *     public void handle(Message message) {
 *         // логика команды
 *     }
 * }
 * }
 * </pre>
 *
 * <p>Пример реализации для диалоговой команды (с учетом состояния):</p>
 * <pre>
 * {@code
 * @Component
 * public class MeetingCreationHandler implements CommandHandler {
 *     @Override
 *     public boolean canHandle(String text, Long chatId) {
 *         return stateService.isCreatingMeeting(chatId);
 *     }
 *
 *     @Override
 *     public void handle(Message message) {
 *         // логика диалога
 *     }
 * }
 * }
 * </pre>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see CommandDispatcher
 * @see UpdateHandler
 */
public interface CommandHandler {

    /**
     * Обрабатывает сообщение пользователя.
     * <p>
     * Вызывается только если один из методов {@link #canHandle} вернул {@code true}.
     * </p>
     *
     * @param message сообщение от пользователя
     */
    void handle(Message message);

    /**
     * Определяет, может ли этот обработчик обработать данное сообщение (без учета chatId).
     * <p>
     * Используется для простых команд, не зависящих от состояния пользователя.
     * По умолчанию возвращает {@code false}.
     * </p>
     *
     * @param text текст сообщения от пользователя
     * @return {@code true}, если обработчик может обработать сообщение,
     *         {@code false} в противном случае
     */
    default boolean canHandle(String text) {
        return false;
    }

    /**
     * Определяет, может ли этот обработчик обработать данное сообщение с учетом chatId.
     * <p>
     * Используется для диалоговых команд, где решение зависит от состояния пользователя.
     * По умолчанию вызывает {@link #canHandle(String)} для обратной совместимости.
     * </p>
     *
     * @param text   текст сообщения от пользователя
     * @param chatId идентификатор чата (нужен для диалоговых команд)
     * @return {@code true}, если обработчик может обработать сообщение,
     *         {@code false} в противном случае
     */
    default boolean canHandle(String text, Long chatId) {
        return canHandle(text);
    }
}