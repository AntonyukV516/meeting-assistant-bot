package com.antonyukV516.bot;

import com.antonyukV516.bot.handler.CallbackQueryHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Обработчик входящих обновлений от Telegram.
 * <p>
 * Выполняет первичную маршрутизацию:
 * <ul>
 *   <li>Callback'и от инлайн-кнопок → {@link CallbackQueryHandler}</li>
 *   <li>Текстовые сообщения → {@link CommandDispatcher}</li>
 *   <li>Остальные типы игнорируются</li>
 * </ul>
 * </p>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see CallbackQueryHandler
 * @see CommandDispatcher
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateHandler {

    private final CommandDispatcher commandDispatcher;
    private final CallbackQueryHandler callbackHandler;

    /**
     * Обрабатывает обновление от Telegram.
     *
     * @param update обновление от Telegram API
     */
    public void handle(Update update) {
        // 1. Логируем тип обновления
        log.info("🔍 Update received. hasCallbackQuery: {}, hasMessage: {}",
                update.hasCallbackQuery(),
                update.hasMessage());

        // 2. Если есть callback — логируем данные
        if (update.hasCallbackQuery()) {
            CallbackQuery callback = update.getCallbackQuery();
            log.info("📞 Callback data: '{}' from user {}",
                    callback.getData(),
                    callback.getFrom().getUserName());
        }

        // 3. Проверяем canHandle
        boolean canHandle = callbackHandler.canHandle(update);
        log.info("🤔 callbackHandler.canHandle(update) = {}", canHandle);

        if (canHandle) {
            log.info("✅ Передаем в CallbackQueryHandler");
            callbackHandler.handle(update);
            return;
        }

        // 4. Обычное сообщение
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("💬 Текстовое сообщение: '{}'", update.getMessage().getText());
            commandDispatcher.dispatch(update.getMessage());
        } else {
            log.info("⏭️ Обновление проигнорировано");
        }
    }
}