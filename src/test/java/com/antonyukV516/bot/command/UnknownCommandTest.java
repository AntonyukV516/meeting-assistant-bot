package com.antonyukV516.bot.command;

import com.antonyukV516.bot.TelegramBot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnknownCommandTest {

    @Mock
    Message message;

    @InjectMocks
    UnknownCommand unknownCommand;

    @Test
    @DisplayName("✅ Должен всегда возвращать true")
    void canHandle_ShouldAlwaysReturnTrue() {
        // given
        // (ничего не надо)

        // when
        boolean result = unknownCommand.canHandle("any text", 123L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("✅ Должен отправить сообщение справку")
    void handle_ShouldSendHelpMessage() {
        try (MockedStatic<TelegramBot> telegramBotMock = mockStatic(TelegramBot.class)) {
            // given
            when(message.getChatId()).thenReturn(123L);

            // when
            unknownCommand.handle(message);

            // then
            telegramBotMock.verify(() -> {
                TelegramBot.send(eq(123L), argThat(text ->
                        text.contains("Я не понимаю такую команду") &&
                                text.contains("/start") &&
                                text.contains("/new")
                ));
            });
        }
    }
}