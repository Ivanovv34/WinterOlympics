package com.example.winter_olympics.user.dto;

import com.example.winter_olympics.common.constants.ValidationMessages;
import com.example.winter_olympics.user.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = ValidationMessages.USERNAME_REQUIRED)
        @Size(min = 3, max = 100, message = ValidationMessages.USERNAME_LENGTH)
        String username,

        @NotBlank(message = ValidationMessages.PASSWORD_REQUIRED)
        @Size(min = 6, message = ValidationMessages.PASSWORD_LENGTH)
        String password,

        @NotNull(message = ValidationMessages.ROLE_REQUIRED)
        Role role
) {
}