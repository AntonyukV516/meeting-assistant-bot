package com.antonyukV516.bot.handler;

import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.bot.state.PendingMeeting;
import com.antonyukV516.bot.state.UserState;
import com.antonyukV516.bot.state.UserStateService;
import com.antonyukV516.model.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Обработчик callback-запросов от инлайн-кнопок.
 * <p>
 * Отвечает за обработку нажатий на:
 * <ul>
 *   <li>Кнопки выбора тегов (префикс {@code tag_})</li>
 *   <li>Кнопку завершения выбора тегов ({@code tags_done})</li>
 *   <li>Кнопку присоединения к встрече (префикс {@code join_})</li>
 * </ul>
 * </p>
 *
 * @author AntonyukV516
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackQueryHandler {

    private final UserStateService stateService;
    private final KeyboardFactory keyboardFactory;

    /**
     * Определяет, является ли обновление callback-запросом.
     *
     * @param update обновление от Telegram
     * @return {@code true} если есть callback
     */
    public boolean canHandle(Update update) {
        boolean result = update.hasCallbackQuery();
        log.info("🔧 CallbackQueryHandler.canHandle() = {}", result);
        return result;
    }

    /**
     * Обрабатывает callback-запрос.
     *
     * @param update обновление с callback
     */
    public void handle(Update update) {
        log.info("🎯 CallbackQueryHandler.handle() начал работу");
        CallbackQuery callback = update.getCallbackQuery();
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();
        Integer messageId = callback.getMessage().getMessageId();

        log.info("Callback from {}: {}", chatId, data);

        if (data.startsWith("join_")) {
            handleJoin(chatId, data);
        } else if (data.startsWith("tag_")) {
            handleTagSelection(chatId, messageId, data);
        } else if ("tags_done".equals(data)) {
            handleTagsDone(chatId, messageId);
        }
    }

    private void handleTagSelection(Long chatId, Integer messageId, String data) {
        String tagName = data.replace("tag_", "");
        log.info("1️⃣ НАЖАТИЕ НА ТЕГ: {} от chatId: {}", tagName, chatId);

        try {
            Tag tag = Tag.valueOf(tagName);
            log.info("2️⃣ ТЕГ РАСПОЗНАН: {}", tag);

            PendingMeeting pending = stateService.getPendingMeeting(chatId);
            if (pending == null) {
                log.warn("⚠️ Нет PendingMeeting для chatId: {}", chatId);
                TelegramBot.send(chatId, "❌ Сессия создания встречи истекла. Начните заново: /new");
                return;
            }

            log.info("3️⃣ ТЕКУЩИЕ ТЕГИ: {}", pending.getTags());

            Set<Tag> selected = new HashSet<>(pending.getTags());
            if (selected.contains(tag)) {
                selected.remove(tag);
            } else {
                selected.add(tag);
            }

            pending.setTags(new ArrayList<>(selected));
            stateService.updatePendingMeeting(chatId, pending);

            TelegramBot.editMessageKeyboard(chatId, messageId,
                    keyboardFactory.createTagSelectionKeyboard(selected));

        } catch (IllegalArgumentException e) {
            log.error("❌ Ошибка: неверный тег {}", tagName);
        }
    }

    private void handleTagsDone(Long chatId, Integer messageId) {
        PendingMeeting pending = stateService.getPendingMeeting(chatId);
        if (pending == null) {
            TelegramBot.send(chatId, "❌ Сессия создания встречи истекла. Начните заново: /new");
            return;
        }

        // Удаляем сообщение с клавиатурой
        TelegramBot.deleteMessage(chatId, messageId);

        // Переходим к следующему шагу
        stateService.setState(chatId, UserState.CREATING_MEETING_DATE);

        String tagsText = pending.getTags().isEmpty()
                ? "не выбраны"
                : pending.getTags().stream()
                .map(Tag::name)
                .collect(Collectors.joining(", "));

        TelegramBot.send(chatId,
                "✅ Теги сохранены: *" + tagsText + "*\n\n" +
                        "------------------------\n" +
                        "**Шаг 4 из 7:**\n" +
                        "Введите **дату и время** встречи\n\n" +
                        "Формат: ДД.ММ.ГГГГ ЧЧ:ММ\n" +
                        "Например: *25.12.2025 15:30*\n\n" +
                        "Чтобы пропустить, отправьте /skip");
    }

    private void handleJoin(Long chatId, String data) {
        String meetingIdStr = data.replace("join_", "");

        try {
            UUID meetingId = UUID.fromString(meetingIdStr);

            // TODO: Здесь будет логика записи на встречу
            // Пока просто подтверждение
            TelegramBot.send(chatId,
                    "✅ Вы записаны на встречу!\n\n" +
                            "Функция записи пока в разработке, но мы уже знаем о вашем желании 😊");

            log.info("User {} wants to join meeting {}", chatId, meetingId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid meeting ID in callback: {}", meetingIdStr);
            TelegramBot.send(chatId, "❌ Ошибка: некорректный ID встречи");
        }
    }
}