package com.example.winter_olympics.user.dto;

import com.example.winter_olympics.user.model.Role;

public record AuthResponse(
        Long id,
        String username,
        Role role
) {
}