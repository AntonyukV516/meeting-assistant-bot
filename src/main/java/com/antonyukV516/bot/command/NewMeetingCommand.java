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

@Component
@RequiredArgsConstructor
@Slf4j
public class NewMeetingCommand implements CommandHandler {

    private final UserStateService stateService;

    @Override
    public boolean canHandle(String text) {
        return "/new".equals(text) ||
                "/new_meeting".equals(text) ||
                "📝 Создать встречу".equals(text);
    }

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
