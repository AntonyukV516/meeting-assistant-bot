package com.antonyukV516.service;

import com.antonyukV516.dto.UserDto;
import com.antonyukV516.mapper.UserMapper;
import com.antonyukV516.model.TelegramUser;
import com.antonyukV516.model.User;
import com.antonyukV516.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private TelegramUser telegramUser;
    private User user;
    private UserDto userDto;
    private final String USERNAME = "testuser";
    private final Long CHAT_ID = 123456789L;

    @BeforeEach
    void setUp() {
        telegramUser = new TelegramUser();
        telegramUser.setUserName(USERNAME);
        telegramUser.setId(987654321L);

        user = User.builder()
                .telegramUsername(USERNAME)
                .chatId(CHAT_ID)
                .build();

        userDto = UserDto.builder()
                .telegramUsername(USERNAME)
                .build();
    }

    @Test
    @DisplayName("✅ Должен создать нового пользователя, если его нет в БД")
    void findOrCreateUser_ShouldCreateNewUser_WhenNotFound() {
        when(userRepository.findById(USERNAME)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.findOrCreateUser(telegramUser, CHAT_ID);

        assertThat(result).isNotNull();
        assertThat(result.getTelegramUsername()).isEqualTo(USERNAME);
        assertThat(result.getChatId()).isEqualTo(CHAT_ID);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("✅ Должен обновить chatId у существующего пользователя")
    void findOrCreateUser_ShouldUpdateChatId_WhenUserExistsAndChatIdChanged() {
        User existingUser = User.builder()
                .telegramUsername(USERNAME)
                .chatId(111111L)
                .build();

        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.findOrCreateUser(telegramUser, CHAT_ID);

        assertThat(result.getChatId()).isEqualTo(CHAT_ID);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("✅ Должен НЕ обновлять chatId, если он не изменился")
    void findOrCreateUser_ShouldNotUpdateChatId_WhenSameChatId() {
        User existingUser = User.builder()
                .telegramUsername(USERNAME)
                .chatId(CHAT_ID)
                .build();

        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(existingUser));

        User result = userService.findOrCreateUser(telegramUser, CHAT_ID);

        assertThat(result.getChatId()).isEqualTo(CHAT_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("✅ Должен выбросить исключение, если username пустой")
    void findOrCreateUser_ShouldThrowException_WhenUsernameEmpty() {
        telegramUser.setUserName(null);

        assertThatThrownBy(() -> userService.findOrCreateUser(telegramUser, CHAT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Telegram username не может быть пустым");

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ Должен найти пользователя по username")
    void findByUsername_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername(USERNAME);

        assertThat(result).isPresent();
        assertThat(result.get().getTelegramUsername()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("✅ Должен вернуть пустой Optional, если пользователь не найден по username")
    void findByUsername_ShouldReturnEmpty_WhenNotFound() {
        when(userRepository.findById(USERNAME)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(USERNAME);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ Должен найти пользователя по chatId")
    void findByChatId_ShouldReturnUser_WhenExists() {
        when(userRepository.findByChatId(CHAT_ID)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByChatId(CHAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getChatId()).isEqualTo(CHAT_ID);
    }

    @Test
    @DisplayName("✅ Должен вернуть пустой Optional, если пользователь не найден по chatId")
    void findByChatId_ShouldReturnEmpty_WhenNotFound() {
        when(userRepository.findByChatId(CHAT_ID)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByChatId(CHAT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ Должен проверить существование пользователя")
    void existsByUsername_ShouldReturnTrue_WhenExists() {
        when(userRepository.existsById(USERNAME)).thenReturn(true);

        boolean result = userService.existsByUsername(USERNAME);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("✅ Должен получить UserDto по username")
    void getUserDto_ShouldReturnDto_WhenUserExists() {
        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserDto(USERNAME);

        assertThat(result).isNotNull();
        assertThat(result.getTelegramUsername()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("✅ Должен вернуть null, если пользователь не найден при запросе DTO")
    void getUserDto_ShouldReturnNull_WhenUserNotFound() {
        when(userRepository.findById(USERNAME)).thenReturn(Optional.empty());

        UserDto result = userService.getUserDto(USERNAME);

        assertThat(result).isNull();
        verify(userMapper, never()).toDto(any());
    }
}
