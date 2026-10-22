package com.itamarprojects.bookorderapp.auth;

import com.itamarprojects.bookorderapp.users.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterRequest(

        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password,
        @NotNull
        UserRole userRole,
        UUID managerId
){ }