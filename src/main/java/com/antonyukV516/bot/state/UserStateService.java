package com.antonyukV516.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для управления состояниями пользователей в диалогах.
 * <p>
 * Хранит в памяти:
 * <ul>
 *   <li>Текущее состояние каждого пользователя ({@link UserState})</li>
 *   <li>Временные данные создаваемой встречи ({@link PendingMeeting})</li>
 * </ul>
 * Использует потокобезопасные {@link ConcurrentHashMap} для работы в многопоточной среде.
 * </p>
 *
 * @author AntonyukV516
 * @version 1.0
 * @see UserState
 * @see PendingMeeting
 * @see com.antonyukV516.bot.handler.MeetingCreationHandler
 */
@Service
@Slf4j
public class UserStateService {

    /** Хранилище состояний пользователей (chatId -> состояние) */
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    /** Хранилище временных данных встреч (chatId -> данные) */
    private final Map<Long, PendingMeeting> pendingMeetings = new ConcurrentHashMap<>();

    /**
     * Возвращает текущее состояние пользователя.
     *
     * @param chatId идентификатор чата
     * @return состояние пользователя или {@link UserState#NONE} если не в диалоге
     */
    public UserState getState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.NONE);
    }

    /**
     * Устанавливает состояние пользователя.
     *
     * @param chatId идентификатор чата
     * @param state  новое состояние
     */
    public void setState(Long chatId, UserState state) {
        if (state == UserState.NONE) {
            userStates.remove(chatId);
        } else {
            userStates.put(chatId, state);
        }
        log.debug("User {} state set to {}", chatId, state);
    }

    /**
     * Сбрасывает состояние пользователя и удаляет временные данные.
     *
     * @param chatId идентификатор чата
     */
    public void resetState(Long chatId) {
        userStates.remove(chatId);
        pendingMeetings.remove(chatId);
        log.debug("User {} state reset", chatId);
    }

    /**
     * Возвращает временные данные создаваемой встречи.
     *
     * @param chatId идентификатор чата
     * @return данные встречи или {@code null} если пользователь не создает встречу
     */
    public PendingMeeting getPendingMeeting(Long chatId) {
        return pendingMeetings.get(chatId);
    }

    /**
     * Сохраняет временные данные создаваемой встречи.
     *
     * @param chatId  идентификатор чата
     * @param meeting данные встречи
     */
    public void setPendingMeeting(Long chatId, PendingMeeting meeting) {
        pendingMeetings.put(chatId, meeting);
    }

    /**
     * Обновляет временные данные создаваемой встречи.
     *
     * @param chatId  идентификатор чата
     * @param meeting обновленные данные
     */
    public void updatePendingMeeting(Long chatId, PendingMeeting meeting) {
        pendingMeetings.put(chatId, meeting);
    }

    /**
     * Проверяет, находится ли пользователь в процессе создания встречи.
     *
     * @param chatId идентификатор чата
     * @return {@code true} если пользователь создает встречу
     */
    public boolean isCreatingMeeting(Long chatId) {
        UserState state = getState(chatId);
        return state != UserState.NONE;
    }
}