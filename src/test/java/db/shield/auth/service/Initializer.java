package db.shield.auth.service;

import db.shield.auth.service.dto.user.UserCreateRequest;
import db.shield.auth.service.dto.user.UserUpdateRequest;
import db.shield.auth.service.model.UserEntity;
import db.shield.auth.service.model.constant.UserRole;
import org.junit.jupiter.api.BeforeAll;

import java.time.Instant;
import java.time.OffsetDateTime;

public abstract class Initializer {

    protected static UserCreateRequest userCreateRequest;
    protected static UserUpdateRequest userUpdateRequest;
    protected static UserEntity userEntity;

    protected static final Long USER_ID = 1L;
    protected static final String USERNAME = "test_user";

    @BeforeAll
    static void init() {
        userCreateRequest = new UserCreateRequest(
                USERNAME,
                "test@mail.com",
                "password123",
                UserRole.READ_ONLY
        );

        userUpdateRequest = new UserUpdateRequest(
                USERNAME,
                "updated@mail.com",
                UserRole.READ_WRITE,
                true
        );

        userEntity = new UserEntity();
        userEntity.setId(USER_ID);
        userEntity.setUsername(USERNAME);
        userEntity.setEmail("test@mail.com");
        userEntity.setPasswordHash("hashed");
        userEntity.setRole(UserRole.READ_ONLY);
        userEntity.setIsLocked(false);
        userEntity.setFailedAttempts(0);
        userEntity.setCreatedAt(Instant.now());
        userEntity.setUpdatedAt(Instant.now());
        userEntity.setLastLoginAt(OffsetDateTime.now());
    }
}
