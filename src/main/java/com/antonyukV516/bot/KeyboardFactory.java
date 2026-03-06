package com.antonyukV516.bot;

import com.antonyukV516.model.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

/**
 * Фабрика для создания клавиатур Telegram.
 * <p>
 * Содержит методы для создания различных типов клавиатур:
 * <ul>
 *   <li>Главное меню (ReplyKeyboard)</li>
 *   <li>Клавиатуры для диалогов (Пропустить/Отмена)</li>
 *   <li>Инлайн-клавиатуры для выбора тегов</li>
 *   <li>Кнопки присоединения к встречам</li>
 * </ul>
 * </p>
 *
 * @author AntonyukV516
 * @version 1.0
 */
@Component
@Slf4j
public class KeyboardFactory {

    /**
     * Создает главное меню с кнопками:
     * <ul>
     *   <li>📝 Создать встречу</li>
     *   <li>📋 Мои встречи</li>
     *   <li>❓ Помощь</li>
     * </ul>
     *
     * @return клавиатура главного меню
     */
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

    /**
     * Создает клавиатуру для пропуска шага.
     *
     * @return клавиатура с кнопками "⏭️ Пропустить" и "❌ Отмена"
     */
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

    /**
     * Создает клавиатуру подтверждения.
     *
     * @return клавиатура с кнопками "✅ Подтвердить" и "❌ Отмена"
     */
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

    /**
     * Создает инлайн-клавиатуру для выбора тегов.
     * <p>
     * Теги группируются по 3 в ряд. Выбранные теги отмечаются ✅.
     * </p>
     *
     * @param selectedTags множество уже выбранных тегов
     * @return инлайн-клавиатура со всеми тегами и кнопкой "✅ ГОТОВО"
     */
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
                log.info("🔘 Создана кнопка: текст='{}', callbackData='{}'", buttonText, callbackData);

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

    /**
     * Возвращает эмодзи для тега.
     *
     * @param tag тег
     * @return соответствующий эмодзи
     */
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

    /**
     * Возвращает отображаемое имя тега на русском языке.
     *
     * @param tag тег
     * @return имя тега на русском, например "Кофе", "Прогулка" и т.д.
     */
    private String getTagDisplayName(Tag tag) {
        return switch (tag) {
            case COFFEE -> "Кофе";
            case WALK -> "Прогулка";
            case SPORT -> "Спорт";
            case FOOD -> "Еда";
            case MOVIE -> "Кино";
            case GAMES -> "Игры";
            case STUDY -> "Учеба";
            case WORK -> "Работа";
            case MUSIC -> "Музыка";
            case ART -> "Искусство";
            case TRAVEL -> "Путешествия";
            case BUSINESS -> "Бизнес";
            case TECH -> "Технологии";
            case BOOKS -> "Книги";
            case BAR -> "Бар";
            case LANGUAGE -> "Языки";
            default -> tag.name().toLowerCase();
        };
    }

    /**
     * Создает инлайн-кнопку для присоединения к встрече.
     *
     * @param meetingId UUID встречи
     * @return инлайн-клавиатура с одной кнопкой "✅ ПРИСОЕДИНИТЬСЯ"
     */
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