package com.antonyukV516.bot.handler;

import com.antonyukV516.bot.CommandHandler;
import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.bot.state.PendingMeeting;
import com.antonyukV516.bot.state.UserState;
import com.antonyukV516.bot.state.UserStateService;
import com.antonyukV516.dto.CreateMeetingDto;
import com.antonyukV516.model.Tag;
import com.antonyukV516.service.MeetingService;
import com.antonyukV516.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Обработчик диалога создания встречи.
 * <p>
 * Реализует 7-шаговый процесс создания встречи:
 * <ol>
 *   <li>Ввод названия</li>
 *   <li>Ввод описания (опционально)</li>
 *   <li>Выбор тегов через инлайн-кнопки</li>
 *   <li>Ввод даты и времени</li>
 *   <li>Ввод места (опционально)</li>
 *   <li>Ввод максимального количества участников (опционально)</li>
 *   <li>Подтверждение и создание встречи</li>
 * </ol>
 * </p>
 *
 * <p>Управляет состояниями через {@link UserStateService} и хранит временные данные
 * в {@link PendingMeeting}. После успешного создания отправляет уведомления через
 * {@link NotificationService}.</p>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see UserStateService
 * @see PendingMeeting
 * @see MeetingService
 * @see NotificationService
 * @see KeyboardFactory
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MeetingCreationHandler implements CommandHandler {

    private final UserStateService stateService;
    private final MeetingService meetingService;
    private final NotificationService notificationService;
    private final KeyboardFactory keyboardFactory;

    /**
            * Форматы даты, которые понимает бот.
            * <p>
     * Поддерживаются форматы:
            * <ul>
     *   <li>{@code dd.MM.yyyy HH:mm}</li>
            *   <li>{@code dd-MM-yyyy HH:mm}</li>
            *   <li>{@code dd/MM/yyyy HH:mm}</li>
            *   <li>{@code yyyy-MM-dd HH:mm}</li>
            *   <li>{@code dd.MM.yy HH:mm}</li>
            *   <li>{@code dd.MM HH:mm} (год будет текущий)</li>
            * </ul>
            * </p>
            */
    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM HH:mm")  // Год будет текущий
    };

    /**
     * {@inheritDoc}
     * <p>
     * Обработчик срабатывает только если пользователь находится в процессе создания встречи
     * (состояние не {@link UserState#NONE}).
     * </p>
     *
     * @param text   текст сообщения
     * @param chatId идентификатор чата
     * @return {@code true} если пользователь создает встречу
     */
    @Override
    public boolean canHandle(String text, Long chatId) {
        return stateService.isCreatingMeeting(chatId);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Основной метод обработки диалога. В зависимости от текущего состояния пользователя
     * вызывает соответствующий метод:
     * <ul>
     *   <li>{@link UserState#CREATING_MEETING_TITLE} → {@link #handleTitle}</li>
     *   <li>{@link UserState#CREATING_MEETING_DESC} → {@link #handleDescription}</li>
     *   <li>{@link UserState#CREATING_MEETING_TAGS} → {@link #handleTags}</li>
     *   <li>{@link UserState#CREATING_MEETING_DATE} → {@link #handleDateTime}</li>
     *   <li>{@link UserState#CREATING_MEETING_LOC} → {@link #handleLocation}</li>
     *   <li>{@link UserState#CREATING_MEETING_MAX} → {@link #handleMaxPeople}</li>
     *   <li>{@link UserState#CONFIRM_MEETING} → {@link #handleConfirm}</li>
     * </ul>
     * </p>
     *
     * @param message сообщение от пользователя
     */
    @Override
    public void handle(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText().trim();

        // Отмена
        if ("/cancel".equals(text) || "❌ Отмена".equals(text)) {
            cancelCreation(chatId);
            return;
        }

        UserState currentState = stateService.getState(chatId);
        PendingMeeting pending = stateService.getPendingMeeting(chatId);

        if (pending == null) {
            stateService.resetState(chatId);
            TelegramBot.send(chatId, "❌ Ошибка. Начните заново: /new");
            return;
        }

        switch (currentState) {
            case CREATING_MEETING_TITLE:
                handleTitle(chatId, text, pending);
                break;
            case CREATING_MEETING_DESC:
                handleDescription(chatId, text, pending);
                break;
            case CREATING_MEETING_TAGS:
                handleTags(chatId, text, pending);
                break;
            case CREATING_MEETING_DATE:
                handleDateTime(chatId, text, pending);
                break;
            case CREATING_MEETING_LOC:
                handleLocation(chatId, text, pending);
                break;
            case CREATING_MEETING_MAX:
                handleMaxPeople(chatId, text, pending);
                break;
            case CONFIRM_MEETING:
                handleConfirm(chatId, text, pending);
                break;
            default:
                stateService.resetState(chatId);
                TelegramBot.send(chatId, "❌ Что-то пошло не так. Начните заново: /new");
        }
    }

    private void handleTitle(Long chatId, String title, PendingMeeting pending) {
        if (title.length() < 3) {
            TelegramBot.send(chatId, "❌ Название должно быть не короче 3 символов. Попробуйте еще раз:");
            return;
        }
        if (title.length() > 50) {
            TelegramBot.send(chatId, "❌ Название не должно превышать 50 символов. Попробуйте еще раз:");
            return;
        }

        pending.setTitle(title);
        stateService.updatePendingMeeting(chatId, pending);
        stateService.setState(chatId, UserState.CREATING_MEETING_DESC);

        TelegramBot.sendWithKeyboard(chatId,
                "✅ Название сохранено: *" + title + "*\n\n" +
                        "------------------------\n" +
                        "**Шаг 2 из 7:**\n" +
                        "Введите **описание встречи** (необязательно)\n\n" +
                        "Чтобы пропустить, нажмите кнопку ниже или отправьте /skip",
                keyboardFactory.createSkipKeyboard());
    }

    private void handleDescription(Long chatId, String description, PendingMeeting pending) {
        if ("⏭️ Пропустить".equals(description) || "/skip".equals(description)) {
            pending.setDescription(null);
            stateService.updatePendingMeeting(chatId, pending);
            stateService.setState(chatId, UserState.CREATING_MEETING_TAGS);

            showTagSelection(chatId, pending);
            return;
        }

        if (description.length() > 1000) {
            TelegramBot.send(chatId, "❌ Описание слишком длинное (макс. 1000 символов). Попробуйте еще раз:");
            return;
        }

        pending.setDescription(description);
        stateService.updatePendingMeeting(chatId, pending);
        stateService.setState(chatId, UserState.CREATING_MEETING_TAGS);

        showTagSelection(chatId, pending);
    }

    private void handleTags(Long chatId, String input, PendingMeeting pending) {
        // Если пользователь пишет текст вместо нажатия на кнопки
        if (!input.startsWith("/") && !input.equals("❌ Отмена")) {
            TelegramBot.send(chatId,
                    "❌ На этом шаге нельзя вводить текст.\n\n" +
                            "👉 **Нажимайте на кнопки ниже**, чтобы выбрать теги.\n" +
                            "✅ ГОТОВО — когда закончите.\n" +
                            "❌ Отмена — отменить создание.");

            // Повторно показываем клавиатуру
            showTagSelection(chatId, pending);
            return;
        }

        // Показываем клавиатуру с тегами (если пришли с другого шага)
        showTagSelection(chatId, pending);
    }

    private void handleDateTime(Long chatId, String dateStr, PendingMeeting pending) {
        if ("⏭️ Пропустить".equals(dateStr) || "/skip".equals(dateStr)) {
            pending.setDateTime(null);
            stateService.updatePendingMeeting(chatId, pending);
            stateService.setState(chatId, UserState.CREATING_MEETING_LOC);

            TelegramBot.sendWithKeyboard(chatId,
                    "------------------------\n" +
                            "**Шаг 5 из 7:**\n" +
                            "Введите **место встречи** (необязательно)\n\n" +
                            "Например: *Кафе Уголек, ул. Ленина 10*\n\n" +
                            "Чтобы пропустить, нажмите кнопку ниже",
                    keyboardFactory.createSkipKeyboard());
            return;
        }

        LocalDateTime dateTime = null;
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                dateTime = LocalDateTime.parse(dateStr, format);
                break;
            } catch (DateTimeParseException e) {
                // пробуем следующий формат
            }
        }

        if (dateTime == null) {
            TelegramBot.send(chatId,
                    "❌ Не могу распознать дату. Используйте формат ДД.ММ.ГГГГ ЧЧ:ММ\n" +
                            "Например: *25.12.2025 15:30*");
            return;
        }

        if (dateTime.isBefore(LocalDateTime.now())) {
            TelegramBot.send(chatId, "❌ Дата не может быть в прошлом. Попробуйте еще раз:");
            return;
        }

        pending.setDateTime(dateTime);
        stateService.updatePendingMeeting(chatId, pending);
        stateService.setState(chatId, UserState.CREATING_MEETING_LOC);

        TelegramBot.sendWithKeyboard(chatId,
                "✅ Дата сохранена: *" + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "*\n\n" +
                        "------------------------\n" +
                        "**Шаг 5 из 7:**\n" +
                        "Введите **место встречи** (необязательно)\n\n" +
                        "Чтобы пропустить, нажмите кнопку ниже",
                keyboardFactory.createSkipKeyboard());
    }

    private void handleLocation(Long chatId, String location, PendingMeeting pending) {
        if ("⏭️ Пропустить".equals(location) || "/skip".equals(location)) {
            pending.setLocation(null);
            stateService.updatePendingMeeting(chatId, pending);
            stateService.setState(chatId, UserState.CREATING_MEETING_MAX);

            TelegramBot.sendWithKeyboard(chatId,
                    "------------------------\n" +
                            "**Шаг 6 из 7:**\n" +
                            "Введите **максимальное количество участников** (необязательно)\n\n" +
                            "Например: *10*\n\n" +
                            "Чтобы пропустить, нажмите кнопку ниже",
                    keyboardFactory.createSkipKeyboard());
            return;
        }

        if (location.length() > 100) {
            TelegramBot.send(chatId, "❌ Название места слишком длинное (макс. 100 символов). Попробуйте еще раз:");
            return;
        }

        pending.setLocation(location);
        stateService.updatePendingMeeting(chatId, pending);
        stateService.setState(chatId, UserState.CREATING_MEETING_MAX);

        TelegramBot.sendWithKeyboard(chatId,
                "✅ Место сохранено: *" + location + "*\n\n" +
                        "------------------------\n" +
                        "**Шаг 6 из 7:**\n" +
                        "Введите **максимальное количество участников** (необязательно)\n\n" +
                        "Например: *10*\n\n" +
                        "Чтобы пропустить, нажмите кнопку ниже",
                keyboardFactory.createSkipKeyboard());
    }

    private void handleMaxPeople(Long chatId, String input, PendingMeeting pending) {
        if ("⏭️ Пропустить".equals(input) || "/skip".equals(input)) {
            pending.setMaxPeople(null);
            stateService.updatePendingMeeting(chatId, pending);
            stateService.setState(chatId, UserState.CONFIRM_MEETING);

            showConfirmation(chatId, pending);
            return;
        }

        Integer maxPeople = null;

        if (!input.isBlank()) {
            try {
                maxPeople = Integer.parseInt(input);
                if (maxPeople < 2) {
                    TelegramBot.send(chatId, "❌ Должно быть хотя бы 2 участника. Попробуйте еще раз:");
                    return;
                }
                if (maxPeople > 100) {
                    TelegramBot.send(chatId, "❌ Слишком много (макс. 100). Попробуйте еще раз:");
                    return;
                }
            } catch (NumberFormatException e) {
                TelegramBot.send(chatId, "❌ Введите число. Например: *10*\nИли нажмите кнопку Пропустить");
                return;
            }
        }

        pending.setMaxPeople(maxPeople);
        stateService.updatePendingMeeting(chatId, pending);
        stateService.setState(chatId, UserState.CONFIRM_MEETING);

        showConfirmation(chatId, pending);
    }

    private void handleConfirm(Long chatId, String input, PendingMeeting pending) {
        if ("✅ Подтвердить".equals(input) || "/confirm".equals(input)) {
            try {
                CreateMeetingDto dto = CreateMeetingDto.builder()
                        .title(pending.getTitle())
                        .description(pending.getDescription())
                        .tags(pending.getTags())
                        .dateTime(pending.getDateTime())
                        .location(pending.getLocation())
                        .maxPeople(pending.getMaxPeople())
                        .build();

                var meeting = meetingService.createMeeting(pending.getCreatorUsername(), dto);

                // Сообщение создателю
                TelegramBot.sendWithKeyboard(chatId,
                        "✅ **ВСТРЕЧА УСПЕШНО СОЗДАНА!**\n\n" +
                                "📌 *" + meeting.getTitle() + "*\n" +
                                (meeting.getDateTime() != null ? "📅 " + meeting.getDateTime()
                                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "\n" : "") +
                                (meeting.getLocation() != null ? "📍 " + meeting.getLocation() + "\n" : "") +
                                (pending.getTags() != null && !pending.getTags().isEmpty() ?
                                        "🏷️ " + formatTags(pending.getTags()) + "\n" : ""),
                        keyboardFactory.createMainMenu());

                // РАССЫЛАЕМ УВЕДОМЛЕНИЯ ВСЕМ
                notificationService.notifyAllUsersAboutNewMeeting(meeting);

                stateService.resetState(chatId);

            } catch (Exception e) {
                log.error("Error creating meeting", e);
                TelegramBot.send(chatId, "❌ Ошибка при создании встречи.");
                stateService.resetState(chatId);
            }
        } else if ("❌ Отмена".equals(input) || "/cancel".equals(input)) {
            cancelCreation(chatId);
        } else {
            TelegramBot.send(chatId, "Пожалуйста, подтвердите или отмените создание.");
        }
    }

    private void showTagSelection(Long chatId, PendingMeeting pending) {
        Set<Tag> selected = new HashSet<>(pending.getTags());
        InlineKeyboardMarkup keyboard = keyboardFactory.createTagSelectionKeyboard(selected);

        log.info("📤 Отправляем клавиатуру с тегами в чат {}", chatId);
        log.info("   Количество рядов: {}", keyboard.getKeyboard().size());

        TelegramBot.sendWithInlineKeyboard(
                chatId,
                "**Шаг 3 из 7: Выберите теги**\n\n" +
                        "Нажимайте на теги, чтобы выбрать/отменить.\n" +
                        "Когда закончите, нажмите **ГОТОВО**.",
                keyboard
        );
    }

    private void showConfirmation(Long chatId, PendingMeeting pending) {
        StringBuilder summary = new StringBuilder();
        summary.append("📋 **ПРОВЕРЬТЕ ДАННЫЕ ВСТРЕЧИ**\n\n");
        summary.append("📌 *Название:* ").append(pending.getTitle()).append("\n");
        summary.append("📝 *Описание:* ").append(pending
                .getDescription() != null ? pending.getDescription() : "не указано").append("\n");
        summary.append("🏷️ *Теги:* ").append(pending.getTags()
                .isEmpty() ? "не выбраны" : formatTags(pending.getTags())).append("\n");
        summary.append("📅 *Дата:* ").append(pending.getDateTime() != null ?
                pending.getDateTime().format(DateTimeFormatter
                        .ofPattern("dd.MM.yyyy HH:mm")) : "не указана").append("\n");
        summary.append("📍 *Место:* ").append(pending
                .getLocation() != null ? pending.getLocation() : "не указано").append("\n");
        summary.append("👥 *Макс. участников:* ").append(pending
                .getMaxPeople() != null ? pending.getMaxPeople() : "без ограничений").append("\n\n");
        summary.append("✅ *Всё верно?*");

        TelegramBot.sendWithKeyboard(chatId, summary.toString(),
                keyboardFactory.createConfirmationKeyboard());
    }

    private String formatTags(List<Tag> tags) {
        if (tags.isEmpty()) return "не выбраны";
        return tags.stream()
                .map(tag -> "`" + tag.name() + "`")
                .collect(Collectors.joining(", "));
    }

    private void cancelCreation(Long chatId) {
        stateService.resetState(chatId);
        TelegramBot.sendWithKeyboard(chatId,
                "❌ Создание встречи отменено.",
                keyboardFactory.createMainMenu());
    }
}