package com.timurtokaev.bankaccess.user.dto;

import com.timurtokaev.bankaccess.user.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserUpdateRequest(

        @NotBlank(message = "Employee number must not be empty")
        @Size(
                max = 50,
                message = "Employee number must not exceed 50 characters"
        )
        String employeeNumber,

        @NotBlank(message = "Username must not be empty")
        @Size(
                max = 100,
                message = "Username must not exceed 100 characters"
        )
        String username,

        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email must be valid")
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email,

        @NotBlank(message = "First name must not be empty")
        @Size(
                max = 100,
                message = "First name must not exceed 100 characters"
        )
        String firstName,

        @NotBlank(message = "Last name must not be empty")
        @Size(
                max = 100,
                message = "Last name must not exceed 100 characters"
        )
        String lastName,

        @NotNull(message = "Department ID must not be null")
        UUID departmentId,

        @NotNull(message = "Status must not be null")
        UserStatus status
) {
}