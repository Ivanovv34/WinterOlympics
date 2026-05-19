package com.example.winter_olympics.user.controller;

import com.example.winter_olympics.user.dto.AuthResponse;
import com.example.winter_olympics.user.dto.LoginRequest;
import com.example.winter_olympics.user.dto.RegisterRequest;
import com.example.winter_olympics.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse createdUser = authService.register(request);

        URI location = URI.create("/api/users/" + createdUser.id());

        return ResponseEntity.created(location).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }
}