package db.shield.auth.service.dto.auth;


import java.util.UUID;


public record LoginResponse(

        String accessToken,
        UUID refreshToken
) {
}
