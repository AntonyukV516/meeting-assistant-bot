package com.antonyukV516;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
