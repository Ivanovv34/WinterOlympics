package com.example.winter_olympics.user.dto;

import com.example.winter_olympics.common.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = ValidationMessages.USERNAME_REQUIRED)
        @Size(min = 3, max = 100, message = ValidationMessages.USERNAME_LENGTH)
        String username,

        @NotBlank(message = ValidationMessages.PASSWORD_REQUIRED)
        @Size(min = 6, message = ValidationMessages.PASSWORD_LENGTH)
        String password
) {
}