package com.timurtokaev.bankaccess.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Username must not be empty")
        @Size(
                max = 100,
                message = "Username must not exceed 100 characters"
        )
        String username,

        @NotBlank(message = "Password must not be empty")
        @Size(
                max = 100,
                message = "Password must not exceed 100 characters"
        )
        String password

) {

    @Override
    public String toString() {
        return "LoginRequest[username=<redacted>, password=<redacted>]";
    }
}