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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeetingCreationHandler implements CommandHandler {

    private final UserStateService stateService;
    private final MeetingService meetingService;
    private final NotificationService notificationService;
    private final KeyboardFactory keyboardFactory;

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM HH:mm")  // Год будет текущий
    };

    @Override
    public boolean canHandle(String text, Long chatId) {
        return stateService.isCreatingMeeting(chatId);
    }

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
        if (!"/skip".equals(description) && description.length() > 1000) {
            TelegramBot.send(chatId, "❌ Описание слишком длинное (макс. 1000 символов). Попробуйте еще раз:");
            return;
        }

        pending.setDescription("/skip".equals(description) ? null : description);
        stateService.updatePendingMeeting(chatId, pending);
        stateService.setState(chatId, UserState.CREATING_MEETING_TAGS);

        TelegramBot.send(chatId,
                "✅ Описание сохранено!");
    }

    private void handleTags(Long chatId, String input, PendingMeeting pending) {
        // Показываем клавиатуру с тегами
        TelegramBot.sendWithInlineKeyboard(
                chatId,
                "**Шаг 3 из 7: Выберите теги**\n\n" +
                        "Нажимайте на теги, чтобы выбрать/отменить.\n" +
                        "Когда закончите, нажмите **ГОТОВО**.",
                keyboardFactory.createTagSelectionKeyboard(new HashSet<>(pending.getTags()))
        );

        // Не меняем состояние — ждем callback'ов
    }

    private void handleDateTime(Long chatId, String dateStr, PendingMeeting pending) {
        if ("/skip".equals(dateStr)) {
            pending.setDateTime(null);
            stateService.updatePendingMeeting(chatId, pending);
            stateService.setState(chatId, UserState.CREATING_MEETING_LOC);

            TelegramBot.send(chatId,
                    "------------------------\n" +
                            "**Шаг 5 из 7:**\n" +
                            "Введите **место встречи** (необязательно)\n\n" +
                            "Например: *Кафе Уголек, ул. Ленина 10*\n\n" +
                            "Чтобы пропустить, отправьте /skip");
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

        TelegramBot.send(chatId,
                "✅ Дата сохранена: *" + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "*\n\n" +
                        "------------------------\n" +
                        "**Шаг 5 из 7:**\n" +
                        "Введите **место встречи** (необязательно)\n\n" +
                        "Чтобы пропустить, отправьте /skip");
    }

    private void handleLocation(Long chatId, String location, PendingMeeting pending) {
        if (!"/skip".equals(location) && location.length() > 100) {
            TelegramBot.send(chatId, "❌ Название места слишком длинное (макс. 100 символов). Попробуйте еще раз:");
            return;
        }

        pending.setLocation("/skip".equals(location) ? null : location);
        stateService.updatePendingMeeting(chatId, pending);
        stateService.setState(chatId, UserState.CREATING_MEETING_MAX);

        TelegramBot.send(chatId,
                "✅ Место сохранено!\n\n" +
                        "------------------------\n" +
                        "**Шаг 6 из 7:**\n" +
                        "Введите **максимальное количество участников** (необязательно)\n\n" +
                        "Например: *10*\n\n" +
                        "Чтобы пропустить, отправьте /skip");
    }

    private void handleMaxPeople(Long chatId, String input, PendingMeeting pending) {
        Integer maxPeople = null;

        if (!"/skip".equals(input) && !input.isBlank()) {
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
                TelegramBot.send(chatId, "❌ Введите число. Например: *10*\nИли отправьте /skip");
                return;
            }
        }

        pending.setMaxPeople(maxPeople);
        stateService.updatePendingMeeting(chatId, pending);
        stateService.setState(chatId, UserState.CONFIRM_MEETING);

        // Формируем сводку
        StringBuilder summary = new StringBuilder();
        summary.append("📋 **ПРОВЕРЬТЕ ДАННЫЕ ВСТРЕЧИ**\n\n");
        summary.append("📌 *Название:* ").append(pending.getTitle()).append("\n");

        if (pending.getDescription() != null) {
            summary.append("📝 *Описание:* ").append(pending.getDescription()).append("\n");
        }

        summary.append("🏷️ *Теги:* ")
                .append(pending.getTags().isEmpty() ? "не выбраны" : pending.getTags()).append("\n");

        if (pending.getDateTime() != null) {
            summary.append("📅 *Дата:* ").append(pending.getDateTime()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("\n");
        }

        if (pending.getLocation() != null) {
            summary.append("📍 *Место:* ").append(pending.getLocation()).append("\n");
        }

        summary.append("👥 *Макс. участников:* ")
                .append(pending.getMaxPeople() != null ? pending.getMaxPeople() : "без ограничений")
                .append("\n\n");
        summary.append("✅ *Всё верно?*");

        TelegramBot.sendWithKeyboard(chatId, summary.toString(),
                keyboardFactory.createConfirmationKeyboard());
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

                //  РАССЫЛАЕМ УВЕДОМЛЕНИЯ ВСЕМ
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