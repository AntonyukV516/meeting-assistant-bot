package com.antonyukV516.repository;

import com.antonyukV516.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .telegramUsername("testuser")
                .chatId(123456789L)
                .build();
    }

    @Test
    @DisplayName("Should save user")
    void saveUser() {
        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getTelegramUsername()).isEqualTo("testuser");
        assertThat(savedUser.getChatId()).isEqualTo(123456789L);
    }

    @Test
    @DisplayName("Should find user by username")
    void findByUsername() {
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findById("testuser");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getTelegramUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should find user by chatId")
    void findByChatId() {
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByChatId(123456789L);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getTelegramUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    void findByUsernameNotFound() {
        Optional<User> foundUser = userRepository.findById("nonexistent");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when user not found by chatId")
    void findByChatIdNotFound() {
        Optional<User> foundUser = userRepository.findByChatId(999999999L);

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by username")
    void existsByUsername() {
        userRepository.save(user);

        boolean exists = userRepository.existsById("testuser");
        boolean notExists = userRepository.existsById("nonexistent");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
