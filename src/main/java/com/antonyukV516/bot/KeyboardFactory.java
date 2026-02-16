package com.antonyukV516.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
public class KeyboardFactory {

    public ReplyKeyboardMarkup createMainMenu() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("📝 Создать встречу");
        row1.add("📋 Мои встречи");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("❓ Помощь");

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .selective(false)
                .build();
    }

    public ReplyKeyboardMarkup createSkipKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("⏭️ Пропустить");
        row.add("❌ Отмена");

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup createConfirmationKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("✅ Подтвердить");
        row.add("❌ Отмена");

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }
}