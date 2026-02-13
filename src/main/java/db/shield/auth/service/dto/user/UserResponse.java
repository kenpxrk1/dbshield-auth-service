package db.shield.auth.service.dto.user;

import db.shield.auth.service.model.constant.UserRole;

import java.time.Instant;
import java.time.OffsetDateTime;

public record UserResponse(

        Long id,

        String username,

        String email,

        UserRole role,

        boolean isLocked,

        int failedAttempts,

        OffsetDateTime lastLoginAt,

        Instant createdAt,

        Instant updatedAt
) {
}
