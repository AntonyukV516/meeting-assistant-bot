package com.antonyukV516.bot.command;

import com.antonyukV516.bot.CommandHandler;
import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.model.TelegramUser;
import com.antonyukV516.model.User;
import com.antonyukV516.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

/**
 * Обработчик команды {@code /start}.
 * <p>
 * Выполняет регистрацию нового пользователя или приветствует существующего.
 * После успешной обработки показывает главное меню с кнопками.
 * </p>
 *
 * <p>Команда срабатывает на:</p>
 * <ul>
 *   <li>Текстовую команду {@code /start}</li>
 * </ul>
 *
 * <p>Алгоритм работы:</p>
 * <ol>
 *   <li>Проверяет, есть ли пользователь в БД по username</li>
 *   <li>Если есть — обновляет chatId (на случай если он изменился) и приветствует</li>
 *   <li>Если нет — создает нового пользователя через {@link UserService#findOrCreateUser}</li>
 *   <li>В любом случае показывает главное меню через {@link KeyboardFactory#createMainMenu()}</li>
 * </ol>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see UserService
 * @see KeyboardFactory
 * @see TelegramBot#sendWithKeyboard
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartCommand implements CommandHandler {

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    /**
     * {@inheritDoc}
     * <p>
     * Для данной команды проверяет, является ли текст сообщения {@code "/start"}.
     * </p>
     *
     * @param text текст сообщения от пользователя
     * @return {@code true} если текст равен {@code "/start"}
     */
    @Override
    public boolean canHandle(String text) {
        return "/start".equals(text);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Выполняет регистрацию/приветствие пользователя и показывает главное меню.
     * В случае ошибки отправляет сообщение об ошибке.
     * </p>
     *
     * @param message сообщение от пользователя
     */
    @Override
    public void handle(Message message) {
        Long chatId = message.getChatId();
        var telegramApiUser = message.getFrom();
        String username = telegramApiUser.getUserName();

        try {
            Optional<User> existingUser = userService.findByUsername(username);

            String responseText;
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (!chatId.equals(user.getChatId())) {
                    user.setChatId(chatId);
                    userService.save(user);
                }

                responseText = String.format(
                        "👋 С возвращением, @%s!\n\n✅ Вы уже зарегистрированы.\n\nВыберите действие:",
                        username
                );
            } else {
                TelegramUser telegramUser = TelegramUser.from(telegramApiUser);
                userService.findOrCreateUser(telegramUser, chatId);

                responseText = String.format(
                        "👋 Привет, @%s!\n\n✅ Вы успешно зарегистрированы!\n\nВыберите действие:",
                        username
                );
            }

            TelegramBot.sendWithKeyboard(chatId, responseText, keyboardFactory.createMainMenu());

        } catch (Exception e) {
            log.error("Error handling /start command", e);
            TelegramBot.send(chatId, "❌ Произошла ошибка");
        }
    }
}