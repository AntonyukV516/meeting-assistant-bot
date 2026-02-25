package com.antonyukV516.service;

import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.model.Meeting;
import com.antonyukV516.model.User;
import com.antonyukV516.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserRepository userRepository;
    private final KeyboardFactory keyboardFactory;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Отправляет уведомление о новой встрече всем пользователям, кроме создателя
     */
    public void notifyAllUsersAboutNewMeeting(Meeting meeting) {
        List<User> allUsers = userRepository.findAll();
        String creatorUsername = meeting.getCreator().getTelegramUsername();

        String meetingInfo = formatMeetingInfo(meeting);
        int sentCount = 0;

        for (User user : allUsers) {
            // Не отправляем создателю
            if (user.getTelegramUsername().equals(creatorUsername)) {
                continue;
            }

            if (user.getChatId() != null) {
                boolean sent = sendMeetingNotification(user.getChatId(), meeting, meetingInfo);
                if (sent) sentCount++;
            }
        }

        log.info("Sent notifications about meeting {} to {} users",
                meeting.getId(), sentCount);
    }

    /**
     * Отправляет уведомление конкретному пользователю
     */
    private boolean sendMeetingNotification(Long chatId, Meeting meeting, String meetingInfo) {
        String message = String.format(
                "📢 **НОВАЯ ВСТРЕЧА!**\n\n" +
                        "От: @%s\n" +
                        "%s\n\n" +
                        "👇 Нажмите кнопку, чтобы присоединиться:",
                meeting.getCreator().getTelegramUsername(),
                meetingInfo
        );

        InlineKeyboardMarkup keyboard = keyboardFactory.createJoinButton(meeting.getId());

        TelegramBot.sendWithInlineKeyboard(chatId, message, keyboard);
        return true;
    }

    /**
     * Форматирует информацию о встрече для сообщения
     */
    private String formatMeetingInfo(Meeting meeting) {
        StringBuilder sb = new StringBuilder();

        sb.append("📌 *").append(meeting.getTitle()).append("*\n");

        if (meeting.getDescription() != null && !meeting.getDescription().isEmpty()) {
            sb.append("📝 ").append(meeting.getDescription()).append("\n");
        }

        if (meeting.getDateTime() != null) {
            sb.append("📅 ").append(meeting.getDateTime().format(DATE_FORMATTER)).append("\n");
        }

        if (meeting.getLocation() != null && !meeting.getLocation().isEmpty()) {
            sb.append("📍 ").append(meeting.getLocation()).append("\n");
        }

        if (meeting.getTags() != null && !meeting.getTags().isEmpty()) {
            sb.append("🏷️ ");
            meeting.getTags().forEach(tag -> sb.append("`").append(tag).append("` "));
            sb.append("\n");
        }

        if (meeting.getMaxPeople() != null) {
            sb.append("👥 Макс. участников: ").append(meeting.getMaxPeople()).append("\n");
        }

        return sb.toString();
    }
}