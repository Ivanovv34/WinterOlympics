package com.example.winter_olympics.user.service;

import com.example.winter_olympics.common.constants.ErrorMessages;
import com.example.winter_olympics.common.exception.BadRequestException;
import com.example.winter_olympics.config.JwtService;
import com.example.winter_olympics.user.dto.AuthResponse;
import com.example.winter_olympics.user.dto.LoginRequest;
import com.example.winter_olympics.user.dto.RegisterRequest;
import com.example.winter_olympics.user.model.UserEntity;
import com.example.winter_olympics.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException(ErrorMessages.USERNAME_ALREADY_EXISTS);
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());

        UserEntity savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.INVALID_USERNAME_OR_PASSWORD));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException(ErrorMessages.INVALID_USERNAME_OR_PASSWORD);
        }

        return mapToResponse(user);
    }

    private AuthResponse mapToResponse(UserEntity user) {
        String token = jwtService.generateToken(
                User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build()
        );

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                token
        );
    }
}