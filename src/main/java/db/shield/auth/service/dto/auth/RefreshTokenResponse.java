package db.shield.auth.service.dto.auth;


import java.util.UUID;


public record RefreshTokenResponse(
        String accessToken,
        UUID refreshToken
) {
}
