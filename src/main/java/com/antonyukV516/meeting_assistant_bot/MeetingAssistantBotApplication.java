package com.antonyukV516.meeting_assistant_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class MeetingAssistantBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingAssistantBotApplication.class, args);
	}

}
