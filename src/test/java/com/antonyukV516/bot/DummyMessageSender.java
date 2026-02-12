package com.antonyukV516.bot;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class DummyMessageSender implements MessageSender {

    @Override
    public void sendMessage(Long chatId, String text) {
        System.out.println("TEST MODE: Would send to " + chatId + ": " + text);
    }
}
