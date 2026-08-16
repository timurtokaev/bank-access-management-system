package com.timurtokaev.bankaccess.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RefreshRequest(

        @NotBlank(message = "Refresh token must not be empty")
        @Size(
                min = 43,
                max = 43,
                message = "Refresh token must contain exactly 43 characters"
        )
        @Pattern(
                regexp = "[A-Za-z0-9_-]{43}",
                message = "Refresh token has an invalid format"
        )
        String refreshToken

) {

    @Override
    public String toString() {
        return "RefreshRequest[refreshToken=<redacted>]";
    }
}