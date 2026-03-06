package com.antonyukV516.bot;

import com.antonyukV516.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KeyboardFactory Tests")
class KeyboardFactoryTest {

    private KeyboardFactory keyboardFactory;

    @BeforeEach
    void setUp() {
        keyboardFactory = new KeyboardFactory();
    }

    @Test
    @DisplayName("✅ createMainMenu должен создавать клавиатуру с 2 рядами")
    void createMainMenu_ShouldCreateKeyboardWith2Rows() {
        ReplyKeyboardMarkup keyboard = keyboardFactory.createMainMenu();

        List<KeyboardRow> rows = keyboard.getKeyboard();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get(0).getText()).isEqualTo("📝 Создать встречу");
        assertThat(rows.get(0).get(1).getText()).isEqualTo("📋 Мои встречи");
        assertThat(rows.get(1).get(0).getText()).isEqualTo("❓ Помощь");
        assertThat(keyboard.getResizeKeyboard()).isTrue();
        assertThat(keyboard.getOneTimeKeyboard()).isFalse();
    }

    @Test
    @DisplayName("✅ createSkipKeyboard должен создавать клавиатуру с 2 кнопками")
    void createSkipKeyboard_ShouldCreateKeyboardWith2Buttons() {
        ReplyKeyboardMarkup keyboard = keyboardFactory.createSkipKeyboard();

        List<KeyboardRow> rows = keyboard.getKeyboard();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).hasSize(2);
        assertThat(rows.get(0).get(0).getText()).isEqualTo("⏭️ Пропустить");
        assertThat(rows.get(0).get(1).getText()).isEqualTo("❌ Отмена");
        assertThat(keyboard.getOneTimeKeyboard()).isTrue();
    }

    @Test
    @DisplayName("✅ createConfirmationKeyboard должен создавать клавиатуру с 2 кнопками")
    void createConfirmationKeyboard_ShouldCreateKeyboardWith2Buttons() {
        ReplyKeyboardMarkup keyboard = keyboardFactory.createConfirmationKeyboard();

        List<KeyboardRow> rows = keyboard.getKeyboard();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).hasSize(2);
        assertThat(rows.get(0).get(0).getText()).isEqualTo("✅ Подтвердить");
        assertThat(rows.get(0).get(1).getText()).isEqualTo("❌ Отмена");
        assertThat(keyboard.getOneTimeKeyboard()).isTrue();
    }

    @Test
    @DisplayName("✅ createJoinButton должен создавать инлайн-кнопку с правильным callback")
    void createJoinButton_ShouldCreateInlineButtonWithCorrectCallback() {
        UUID meetingId = UUID.randomUUID();

        InlineKeyboardMarkup keyboard = keyboardFactory.createJoinButton(meetingId);

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).hasSize(1);

        InlineKeyboardButton button = rows.get(0).get(0);
        assertThat(button.getText()).isEqualTo("✅ ПРИСОЕДИНИТЬСЯ");
        assertThat(button.getCallbackData()).isEqualTo("join_" + meetingId);
    }

    @Test
    @DisplayName("✅ createTagSelectionKeyboard должен создавать клавиатуру со всеми тегами")
    void createTagSelectionKeyboard_ShouldCreateKeyboardWithAllTags() {
        Set<Tag> selectedTags = new HashSet<>();
        selectedTags.add(Tag.COFFEE);
        selectedTags.add(Tag.SPORT);

        InlineKeyboardMarkup keyboard = keyboardFactory.createTagSelectionKeyboard(selectedTags);

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();

        // Все теги (16) по 3 в ряд = 6 рядов + 1 ряд с кнопкой ГОТОВО = 7
        assertThat(rows).hasSize(7);

        // Проверяем первый ряд
        List<InlineKeyboardButton> firstRow = rows.get(0);
        assertThat(firstRow).hasSize(3);

        // COFFEE должен быть выбран
        assertThat(firstRow.get(0).getText()).contains("✅");
        assertThat(firstRow.get(0).getText()).contains("☕");
        assertThat(firstRow.get(0).getCallbackData()).isEqualTo("tag_COFFEE");

        // WALK не выбран
        assertThat(firstRow.get(1).getText()).contains("⚪");
        assertThat(firstRow.get(1).getText()).contains("🚶");
        assertThat(firstRow.get(1).getCallbackData()).isEqualTo("tag_WALK");

        // SPORT выбран
        assertThat(firstRow.get(2).getText()).contains("✅");
        assertThat(firstRow.get(2).getText()).contains("⚽");
        assertThat(firstRow.get(2).getCallbackData()).isEqualTo("tag_SPORT");

        // Проверяем последний ряд
        List<InlineKeyboardButton> lastRow = rows.get(rows.size() - 1);
        assertThat(lastRow).hasSize(1);
        assertThat(lastRow.get(0).getText()).isEqualTo("✅ ГОТОВО");
        assertThat(lastRow.get(0).getCallbackData()).isEqualTo("tags_done");
    }

    @Test
    @DisplayName("✅ getTagEmoji должен возвращать правильные эмодзи")
    void getTagEmoji_ShouldReturnCorrectEmoji() {
        // Проверяем через создание клавиатуры
        Set<Tag> emptySet = new HashSet<>();
        InlineKeyboardMarkup keyboard = keyboardFactory.createTagSelectionKeyboard(emptySet);

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();

        // Первый ряд: COFFEE, WALK, SPORT
        assertThat(rows.get(0).get(0).getText()).contains("☕");
        assertThat(rows.get(0).get(1).getText()).contains("🚶");
        assertThat(rows.get(0).get(2).getText()).contains("⚽");

        // Второй ряд: FOOD, MOVIE, GAMES
        assertThat(rows.get(1).get(0).getText()).contains("🍔");
        assertThat(rows.get(1).get(1).getText()).contains("🎬");
        assertThat(rows.get(1).get(2).getText()).contains("🎮");
    }

    @Test
    @DisplayName("✅ getTagDisplayName должен возвращать читаемые названия")
    void getTagDisplayName_ShouldReturnReadableNames() {
        Set<Tag> emptySet = new HashSet<>();
        InlineKeyboardMarkup keyboard = keyboardFactory.createTagSelectionKeyboard(emptySet);

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();

        // Проверяем, что названия с заглавной буквы
        assertThat(rows.get(0).get(0).getText()).contains("Кофе");
        assertThat(rows.get(0).get(1).getText()).contains("Прогулка");
        assertThat(rows.get(0).get(2).getText()).contains("Спорт");
    }
}