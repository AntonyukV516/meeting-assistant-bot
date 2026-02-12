package com.antonyukV516.service;

import com.antonyukV516.dto.UserDto;
import com.antonyukV516.mapper.UserMapper;
import com.antonyukV516.model.TelegramUser;
import com.antonyukV516.model.User;
import com.antonyukV516.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public User findOrCreateUser(TelegramUser telegramUser, Long chatId) {
        String username = telegramUser.getUserName();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Telegram username не может быть пустым");
        }

        return userRepository.findById(username)
                .map(existingUser -> {
                    if (!chatId.equals(existingUser.getChatId())) {
                        existingUser.setChatId(chatId);
                        log.info("Updated chatId for user @{}: {}", username, chatId);
                        return userRepository.save(existingUser);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .telegramUsername(username)
                            .chatId(chatId)
                            .build();
                    log.info("Creating new user: @{} with chatId: {}", username, chatId);
                    return userRepository.save(newUser);
                });
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findById(username);
    }

    public Optional<User> findByChatId(Long chatId) {
        return userRepository.findByChatId(chatId);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsById(username);
    }

    public UserDto getUserDto(String username) {
        return userRepository.findById(username)
                .map(userMapper::toDto)
                .orElse(null);
    }
}