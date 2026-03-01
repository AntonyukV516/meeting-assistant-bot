package com.antonyukV516.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Главный класс Telegram бота.
 * <p>
 * Отвечает за получение обновлений от Telegram API и передачу их в {@link UpdateHandler}.
 * Реализует паттерн Singleton через статическое поле instance для доступа из статических методов.
 * </p>
 *
 * <p>Все сообщения отправляются через статические методы этого класса.</p>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see UpdateHandler
 * @see CommandDispatcher
 */
@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private static TelegramBot instance;

    private final String botToken;
    private final String botUsername;
    private final UpdateHandler updateHandler;

    /**
     * Создает экземпляр бота и сохраняет его в статическом поле.
     *
     * @param botToken       токен бота из application.yml (переменная окружения BOT_TOKEN)
     * @param botUsername    username бота из application.yml (без @)
     * @param updateHandler  обработчик обновлений, который будет получать все события
     */
    public TelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            UpdateHandler updateHandler) {
        super(botToken);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.updateHandler = updateHandler;
        TelegramBot.instance = this;
    }

    /**
     * Возвращает username бота для Telegram API.
     *
     * @return username бота
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Обрабатывает входящее обновление от Telegram.
     * <p>
     * Все типы обновлений (текстовые сообщения, callback'и от кнопок и т.д.)
     * передаются в {@link UpdateHandler} без фильтрации.
     * </p>
     *
     * @param update обновление от Telegram API
     */
    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Received update: {}", update.getUpdateId());

        if (update.hasCallbackQuery()) {
            log.info("📞 Получен callback: {}", update.getCallbackQuery().getData());
        } else if (update.hasMessage()) {
            log.info("💬 Получено сообщение: {}", update.getMessage().getText());
        } else {
            log.info("🔄 Получен другой тип обновления");
        }

        updateHandler.handle(update);
    }

    /**
            * Отправляет простое текстовое сообщение без клавиатуры.
            *
            * @param chatId ID чата получателя
     * @param text   текст сообщения (поддерживается HTML-разметка)
     */
    public static void send(Long chatId, String text) {
        if (instance == null) {
            log.error("TelegramBot instance not initialized");
            return;
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .build();

        try {
            instance.execute(message);
            log.debug("Message sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId: {}", chatId, e);
        }
    }

    /**
     * Отправляет сообщение с обычной клавиатурой (ReplyKeyboard).
     *
     * @param chatId   ID чата получателя
     * @param text     текст сообщения
     * @param keyboard клавиатура для отправки
     */
    public static void sendWithKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        if (instance == null) {
            log.error("TelegramBot instance not initialized");
            return;
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            instance.execute(message);
            log.debug("Message with keyboard sent to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId: {}", chatId, e);
        }
    }

    /**
     * Отправляет сообщение с инлайн-клавиатурой (кнопки под сообщением).
     *
     * @param chatId   ID чата получателя
     * @param text     текст сообщения
     * @param keyboard инлайн-клавиатура
     */
    public static void sendWithInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        if (instance == null) {
            log.error("TelegramBot instance not initialized");
            return;
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            instance.execute(message);
            log.info("✅ Сообщение с инлайн-клавиатурой отправлено в чат {}", chatId);
        } catch (TelegramApiException e) {
            log.error("❌ Ошибка отправки сообщения с клавиатурой в чат {}: {}", chatId, e.getMessage());
        }
    }

    /**
     * Обновляет клавиатуру у существующего сообщения.
     * <p>
     * Используется при выборе тегов, чтобы обновить ✅/⚪ без отправки нового сообщения.
     * </p>
     *
     * @param chatId    ID чата
     * @param messageId ID сообщения для обновления
     * @param keyboard  новая клавиатура
     */
    public static void editMessageKeyboard(Long chatId, Integer messageId, InlineKeyboardMarkup keyboard) {
        if (instance == null) return;

        EditMessageReplyMarkup edit = EditMessageReplyMarkup.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .replyMarkup(keyboard)
                .build();

        try {
            instance.execute(edit);
            log.debug("Keyboard updated for message: {}", messageId);
        } catch (TelegramApiException e) {
            log.error("Failed to edit message keyboard", e);
        }
    }

    /**
     * Удаляет сообщение.
     * <p>
     * Используется после завершения выбора тегов, чтобы убрать сообщение с клавиатурой.
     * </p>
     *
     * @param chatId    ID чата
     * @param messageId ID сообщения для удаления
     */
    public static void deleteMessage(Long chatId, Integer messageId) {
        if (instance == null) return;

        DeleteMessage delete = DeleteMessage.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .build();

        try {
            instance.execute(delete);
            log.debug("Message deleted: {}", messageId);
        } catch (TelegramApiException e) {
            log.error("Failed to delete message", e);
        }
    }


}