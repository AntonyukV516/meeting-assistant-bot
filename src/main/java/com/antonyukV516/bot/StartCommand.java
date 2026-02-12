package com.antonyukV516.bot;

import com.antonyukV516.model.TelegramUser;
import com.antonyukV516.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartCommand implements CommandHandler {

    private final UserService userService;
    private final MessageSender messageSender;

    @Override
    public boolean canHandle(String text) {
        return "/start".equals(text);
    }

    @Override
    public void handle(Message message) {
        Long chatId = message.getChatId();
        var telegramApiUser = message.getFrom();
        TelegramUser telegramUser = TelegramUser.from(telegramApiUser);
        String username = telegramUser.getUserName();

        try {
            userService.findOrCreateUser(telegramUser, chatId);

            String response = String.format(
                    """
                    👋 Привет, @%s!
                    
                    ✅ Вы успешно зарегистрированы!
                    """,
                    username
            );

            messageSender.sendMessage(chatId, response);

        } catch (Exception e) {
            log.error("Error handling /start command", e);
            messageSender.sendMessage(chatId,
                    "❌ Произошла ошибка при обработке команды.\n" +
                            "Пожалуйста, попробуйте позже или обратитесь к администратору."
            );
        }
    }
}