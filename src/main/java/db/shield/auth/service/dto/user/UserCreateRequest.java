package db.shield.auth.service.dto.user;

import db.shield.auth.service.model.constant.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank
        @Size(max = 100)
        String username,

        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 255)
        String password,

        @NotNull
        UserRole role
) {
}
