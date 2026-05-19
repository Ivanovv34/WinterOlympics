package com.example.winter_olympics.user.service;

import com.example.winter_olympics.common.exception.BadRequestException;
import com.example.winter_olympics.config.JwtService;
import com.example.winter_olympics.user.dto.AuthResponse;
import com.example.winter_olympics.user.dto.LoginRequest;
import com.example.winter_olympics.user.dto.RegisterRequest;
import com.example.winter_olympics.user.model.Role;
import com.example.winter_olympics.user.model.UserEntity;
import com.example.winter_olympics.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerShouldCreateUserWhenUsernameIsAvailable() {
        RegisterRequest request = new RegisterRequest(
                "admin",
                "admin123",
                Role.ADMIN
        );

        UserEntity savedUser = new UserEntity();
        savedUser.setId(1L);
        savedUser.setUsername("admin");
        savedUser.setPassword("encoded-password");
        savedUser.setRole(Role.ADMIN);

        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("test-token");

        AuthResponse response = authService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
        assertThat(response.token()).isEqualTo("test-token");

        verify(userRepository).existsByUsername("admin");
        verify(passwordEncoder).encode("admin123");
        verify(userRepository).save(any(UserEntity.class));
        verify(jwtService).generateToken(any());
    }

    @Test
    void registerShouldThrowBadRequestWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "admin",
                "admin123",
                Role.ADMIN
        );

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username already exists");

        verify(userRepository).existsByUsername("admin");
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginShouldReturnUserWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest(
                "admin",
                "admin123"
        );

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("test-token");

        AuthResponse response = authService.login(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
        assertThat(response.token()).isEqualTo("test-token");

        verify(userRepository).findByUsername("admin");
        verify(passwordEncoder).matches("admin123", "encoded-password");
        verify(jwtService).generateToken(any());
    }

    @Test
    void loginShouldThrowBadRequestWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest(
                "admin",
                "wrongpass"
        );

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid username or password");

        verify(userRepository).findByUsername("admin");
        verify(passwordEncoder).matches("wrongpass", "encoded-password");
        verify(jwtService, never()).generateToken(any());
    }
}