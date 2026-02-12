package com.antonyukV516;

import com.antonyukV516.bot.TelegramBot;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class MeetingAssistantBotApplicationTest {

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private TelegramBotsApi telegramBotsApi;

    @Test
    void contextLoad(ApplicationContext context) {
        assertThat(context).isNotNull();
    }

    @Test
    void meetingBotApplicationBeanExists(ApplicationContext context) {
        assertThat(context.containsBean("meetingAssistantBotApplication")).isTrue();
    }
}
