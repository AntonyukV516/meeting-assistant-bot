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

@Component
@RequiredArgsConstructor
@Slf4j
public class StartCommand implements CommandHandler {

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    @Override
    public boolean canHandle(String text) {
        return "/start".equals(text);
    }

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