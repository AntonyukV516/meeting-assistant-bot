package com.antonyukV516.model;

import lombok.Data;

@Data
public class TelegramUser {
    private String userName;
    private Long id;

    public static TelegramUser from(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        TelegramUser user = new TelegramUser();
        user.setUserName(telegramUser.getUserName());
        user.setId(telegramUser.getId());
        return user;
    }
}
