package db.shield.auth.service.dto.auth;


import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record RefreshTokenRequest(

        @NotNull(message = "Refresh token must not be null")
        UUID refreshToken
) {
}
