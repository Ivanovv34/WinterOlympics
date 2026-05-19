package com.example.winter_olympics.user.service;

import com.example.winter_olympics.user.dto.AuthResponse;
import com.example.winter_olympics.user.dto.LoginRequest;
import com.example.winter_olympics.user.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}