package com.antonyukV516.bot.command;

import com.antonyukV516.bot.CommandHandler;
import com.antonyukV516.bot.TelegramBot;
import com.antonyukV516.bot.state.PendingMeeting;
import com.antonyukV516.bot.state.UserState;
import com.antonyukV516.bot.state.UserStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Обработчик команды {@code /new} для создания новой встречи.
 * <p>
 * Запускает диалог создания встречи, переводя пользователя в состояние
 * {@link UserState#CREATING_MEETING_TITLE} и создавая временный объект
 * {@link PendingMeeting} для хранения данных.
 * </p>
 *
 * <p>Команда срабатывает на:</p>
 * <ul>
 *   <li>Текстовую команду {@code /new}</li>
 *   <li>Текстовую команду {@code /new_meeting}</li>
 *   <li>Нажатие кнопки "📝 Создать встречу"</li>
 * </ul>
 *
 * <p>Если пользователь уже находится в диалоге создания, отправляет
 * соответствующее предупреждение.</p>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see com.antonyukV516.bot.state.UserStateService
 * @see com.antonyukV516.bot.handler.MeetingCreationHandler
 * @see com.antonyukV516.bot.state.PendingMeeting
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewMeetingCommand implements CommandHandler {

    private final UserStateService stateService;

    /**
     * {@inheritDoc}
     * <p>
     * Проверяет, является ли текст сообщения одной из команд создания встречи
     * или нажатием соответствующей кнопки.
     * </p>
     *
     * @param text текст сообщения от пользователя
     * @return {@code true} для команд {@code /new}, {@code /new_meeting}
     * или текста кнопки "📝 Создать встречу"; {@code false} в остальных случаях
     */
    @Override
    public boolean canHandle(String text) {
        return "/new".equals(text) ||
                "/new_meeting".equals(text) ||
                "📝 Создать встречу".equals(text);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Запускает диалог создания встречи:
     * <ol>
     *   <li>Проверяет, не начат ли уже диалог через {@link UserStateService#isCreatingMeeting(Long)}</li>
     *   <li>Создает {@link PendingMeeting} с username создателя и chatId</li>
     *   <li>Устанавливает состояние {@link UserState#CREATING_MEETING_TITLE}</li>
     *   <li>Отправляет первое сообщение с запросом названия встречи</li>
     * </ol>
     * </p>
     *
     * @param message сообщение от пользователя
     */
    @Override
    public void handle(Message message) {
        Long chatId = message.getChatId();
        String username = message.getFrom().getUserName();

        // Проверяем, не в диалоге ли уже
        if (stateService.isCreatingMeeting(chatId)) {
            TelegramBot.send(chatId, """
                    ⚠️ Вы уже создаете встречу!
                                        
                    Чтобы продолжить, ответьте на последний вопрос.
                    Чтобы отменить, отправьте /cancel
                    """);
            return;
        }

        // Создаем временный объект
        PendingMeeting pending = PendingMeeting.builder()
                .creatorUsername(username)
                .chatId(chatId)
                .build();

        stateService.setPendingMeeting(chatId, pending);
        stateService.setState(chatId, UserState.CREATING_MEETING_TITLE);

        TelegramBot.send(chatId, """
                📝 **СОЗДАНИЕ НОВОЙ ВСТРЕЧИ**
                                
                Я задам вам несколько вопросов.
                Отменить создание: /cancel
                                
                --------------------
                **Шаг 1 из 7:**
                Введите **название встречи** (обязательно)
                                
                Например: `Поход в кино`
                """);
    }
}
