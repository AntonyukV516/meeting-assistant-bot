package com.antonyukV516.bot.state;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserStateService {

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, PendingMeeting> pendingMeetings = new ConcurrentHashMap<>();

    public UserState getState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.NONE);
    }

    public void setState(Long chatId, UserState state) {
        if (state == UserState.NONE) {
            userStates.remove(chatId);
        } else {
            userStates.put(chatId, state);
        }
        log.debug("User {} state set to {}", chatId, state);
    }

    public void resetState(Long chatId) {
        userStates.remove(chatId);
        pendingMeetings.remove(chatId);
        log.debug("User {} state reset", chatId);
    }

    public PendingMeeting getPendingMeeting(Long chatId) {
        return pendingMeetings.get(chatId);
    }

    public void setPendingMeeting(Long chatId, PendingMeeting meeting) {
        pendingMeetings.put(chatId, meeting);
    }

    public void updatePendingMeeting(Long chatId, PendingMeeting meeting) {
        pendingMeetings.put(chatId, meeting);
    }

    public boolean isCreatingMeeting(Long chatId) {
        UserState state = getState(chatId);
        return state != UserState.NONE;
    }
}