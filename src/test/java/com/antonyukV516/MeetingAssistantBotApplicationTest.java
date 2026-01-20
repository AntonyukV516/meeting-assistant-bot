package com.antonyukV516;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class MeetingAssistantBotApplicationTest {

    @Test
    void contextLoad(ApplicationContext context) {
        assertThat(context).isNotNull();
    }

    @Test
    void meetingBotApplicationBeanExists(ApplicationContext context) {
        assertThat(context.containsBean("meetingAssistantBotApplication")).isTrue();
    }
}
