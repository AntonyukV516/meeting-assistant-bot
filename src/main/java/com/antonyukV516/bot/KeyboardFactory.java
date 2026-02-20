package com.antonyukV516.bot;

import com.antonyukV516.model.Tag;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

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

    public InlineKeyboardMarkup createTagSelectionKeyboard(Set<Tag> selectedTags) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<Tag> allTags = Arrays.asList(Tag.values());
        for (int i = 0; i < allTags.size(); i += 3) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = i; j < Math.min(i + 3, allTags.size()); j++) {
                Tag tag = allTags.get(j);
                boolean isSelected = selectedTags.contains(tag);

                String buttonText = (isSelected ? "✅ " : "⚪ ") + getTagEmoji(tag)
                        + " " + getTagDisplayName(tag);
                String callbackData = "tag_" + tag.name();

                row.add(InlineKeyboardButton.builder()
                        .text(buttonText)
                        .callbackData(callbackData)
                        .build());
            }
            rows.add(row);
        }

        List<InlineKeyboardButton> lastRow = new ArrayList<>();
        lastRow.add(InlineKeyboardButton.builder()
                .text("✅ ГОТОВО")
                .callbackData("tags_done")
                .build());
        rows.add(lastRow);

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private String getTagEmoji(Tag tag) {
        return switch (tag) {
            case COFFEE -> "☕";
            case WALK -> "🚶";
            case SPORT -> "⚽";
            case FOOD -> "🍔";
            case MOVIE -> "🎬";
            case GAMES -> "🎮";
            case STUDY -> "📚";
            case WORK -> "💼";
            case MUSIC -> "🎵";
            case ART -> "🎨";
            case TRAVEL -> "✈️";
            case BUSINESS -> "📊";
            case TECH -> "💻";
            case BOOKS -> "📖";
            case BAR -> "🍷";
            case LANGUAGE -> "🗣️";
            default -> "🔹";
        };
    }

    private String getTagDisplayName(Tag tag) {
        String name = tag.name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public InlineKeyboardMarkup createJoinButton(UUID meetingId) {
        InlineKeyboardButton joinButton = InlineKeyboardButton.builder()
                .text("✅ ПРИСОЕДИНИТЬСЯ")
                .callbackData("join_" + meetingId)
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(joinButton)))
                .build();
    }
}