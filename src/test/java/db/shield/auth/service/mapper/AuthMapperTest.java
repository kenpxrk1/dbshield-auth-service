package db.shield.auth.service.mapper;


import db.shield.auth.service.Initializer;
import db.shield.auth.service.dto.auth.LoginResponse;
import db.shield.auth.service.dto.auth.RefreshTokenResponse;
import db.shield.auth.service.model.TokenEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


class AuthMapperTest extends Initializer {

    private final AuthMapper authMapper = new AuthMapperImpl();

    @Test
    void toTokenEntity_success() {
        UUID refreshToken = UUID.randomUUID();

        TokenEntity token = authMapper.toTokenEntity(userEntity, refreshToken);

        assertNotNull(token);
        assertEquals(userEntity, token.getUser());
        assertEquals(refreshToken, token.getRefreshToken());
        assertFalse(token.isRevoked());
        assertNull(token.getId());
        assertNull(token.getExpireTime());
    }

    @Test
    void toLoginResponse_success() {
        UUID refreshToken = UUID.randomUUID();
        String accessToken = "access-token";

        LoginResponse response =
                authMapper.toLoginResponse(accessToken, refreshToken);

        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshToken, response.refreshToken());
    }

    @Test
    void toRefreshResponse_success() {
        UUID refreshToken = UUID.randomUUID();
        String accessToken = "access-token";

        RefreshTokenResponse response =
                authMapper.toRefreshResponse(accessToken, refreshToken);

        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshToken, response.refreshToken());
    }
}

