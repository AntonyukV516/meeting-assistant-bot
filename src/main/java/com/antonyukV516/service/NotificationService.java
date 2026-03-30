package com.antonyukV516.service;

import com.antonyukV516.bot.KeyboardFactory;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.model.Meeting;
import com.antonyukV516.model.User;
import com.antonyukV516.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для отправки уведомлений пользователям.
 * <p>
 * Реализует как синхронную, так и асинхронную отправку сообщений через Telegram Bot API.
 * Для массовых рассылок используется асинхронный подход с пулом потоков,
 * чтобы не блокировать основной поток обработки команд пользователя.
 * </p>
 *
 * @author AntonyukV516
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserRepository userRepository;
    private final KeyboardFactory keyboardFactory;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Синхронная отправка сообщения пользователю.
     *
     * @param chatId идентификатор чата в Telegram
     * @param text   текст сообщения
     */
    public void sendNotificationSync(Long chatId, String text) {
        TelegramBot.send(chatId, text);
    }

    /**
     * Асинхронная отправка одного уведомления с инлайн-кнопкой.
     * <p>
     * Метод выполняется в отдельном потоке из пула {@code notificationExecutor}.
     * Отправляет сообщение с кнопкой "ПРИСОЕДИНИТЬСЯ".
     * </p>
     *
     * @param chatId    идентификатор чата в Telegram
     * @param meeting   встреча, на которую приглашают
     * @param meetingInfo отформатированная информация о встрече
     * @return CompletableFuture с результатом отправки
     */
    @Async("notificationExecutor")
    public CompletableFuture<Boolean> sendNotificationWithJoinButtonAsync(Long chatId,
                                                                          Meeting meeting,
                                                                          String meetingInfo) {
        log.debug("📤 Отправка уведомления с кнопкой в чат {} (поток: {})",
                chatId, Thread.currentThread().getName());

        try {
            String message = String.format(
                    "📢 **НОВАЯ ВСТРЕЧА!**\n\n" +
                            "От: @%s\n" +
                            "%s",
                    meeting.getCreator().getTelegramUsername(),
                    meetingInfo
            );

            TelegramBot.sendWithInlineKeyboard(
                    chatId,
                    message,
                    keyboardFactory.createJoinButton(meeting.getId())
            );

            log.debug("✅ Уведомление с кнопкой отправлено в чат {}", chatId);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("❌ Ошибка отправки уведомления в чат {}: {}", chatId, e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }


    /**
     * Асинхронная рассылка уведомлений о новой встрече с кнопкой "ПРИСОЕДИНИТЬСЯ".
     * <p>
     * Отправляет сообщение всем зарегистрированным пользователям, кроме создателя встречи.
     * Рассылка выполняется в фоновом режиме, не блокируя основной поток.
     * </p>
     *
     * @param meeting созданная встреча
     */
    public void notifyAllUsersAboutNewMeeting(Meeting meeting) {
        List<User> allUsers = userRepository.findAll();
        String creatorUsername = meeting.getCreator().getTelegramUsername();
        String meetingInfo = formatMeetingInfo(meeting);

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getTelegramUsername().equals(creatorUsername)) {
                continue;
            }

            if (user.getChatId() != null) {
                CompletableFuture<Boolean> future = sendNotificationWithJoinButtonAsync(
                        user.getChatId(), meeting, meetingInfo);
                futures.add(future);
            }
        }

        CompletableFuture.runAsync(() -> logNotificationResults(futures, futures.size()));

        log.info("🚀 Запущена асинхронная рассылка {} уведомлений о встрече '{}'",
                futures.size(), meeting.getTitle());
    }

    /**
     * Фоновое логирование результатов рассылки.
     *
     * @param futures    список CompletableFuture отправок
     * @param totalCount общее количество запланированных отправок
     */
    private void logNotificationResults(List<CompletableFuture<Boolean>> futures, int totalCount) {
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(15, TimeUnit.SECONDS);

            long successCount = futures.stream()
                    .filter(f -> f.getNow(false))
                    .count();

            log.info("✅ Рассылка завершена: {}/{} успешно отправлено", successCount, totalCount);

        } catch (Exception e) {
            log.warn("⚠️ Рассылка частично не удалась: {}", e.getMessage());
        }
    }

    /**
     * Форматирует информацию о встрече для отображения в уведомлении.
     *
     * @param meeting встреча
     * @return отформатированная строка с информацией о встрече
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