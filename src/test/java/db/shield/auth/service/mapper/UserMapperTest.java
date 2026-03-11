package db.shield.auth.service.mapper;


import db.shield.auth.service.Initializer;
import db.shield.auth.service.dto.user.UserResponse;
import db.shield.auth.service.model.UserEntity;
import db.shield.auth.service.model.constant.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


class UserMapperTest extends Initializer {

    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void toEntity_shouldMapCorrectly() {
        UserEntity entity = userMapper.toEntity(userCreateRequest);

        assertThat(entity).isNotNull();
        assertThat(entity.getUsername()).isEqualTo(userCreateRequest.username());
        assertThat(entity.getEmail()).isEqualTo(userCreateRequest.email());
        assertThat(entity.getRole()).isEqualTo(userCreateRequest.role());
        assertThat(entity.getPasswordHash()).isNull();
        assertThat(entity.getFailedAttempts()).isZero();
        assertThat(entity.getIsLocked()).isFalse();
    }

    @Test
    void toEntity_null_shouldReturnNull() {
        UserEntity entity = userMapper.toEntity(null);
        assertThat(entity).isNull();
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        UserResponse response = userMapper.toResponse(userEntity);

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo(userEntity.getUsername());
        assertThat(response.email()).isEqualTo(userEntity.getEmail());
        assertThat(response.role()).isEqualTo(userEntity.getRole());
    }

    @Test
    void toResponse_null_shouldReturnNull() {
        UserResponse response = userMapper.toResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void updateEntity_shouldUpdateOnlyNonNullFields() {
        UserEntity entity = new UserEntity();
        entity.setUsername(USERNAME);
        entity.setEmail("old@mail.com");
        entity.setRole(UserRole.READ_ONLY);
        entity.setIsLocked(false);

        userMapper.updateEntity(userUpdateRequest, entity);

        assertThat(entity.getEmail()).isEqualTo(userUpdateRequest.email());
        assertThat(entity.getRole()).isEqualTo(userUpdateRequest.role());
        assertTrue(entity.getIsLocked());
    }
}
