package db.shield.auth.service.mapper;


import db.shield.auth.service.dto.user.UserCreateRequest;
import db.shield.auth.service.dto.user.UserResponse;
import db.shield.auth.service.dto.user.UserUpdateRequest;
import db.shield.auth.service.model.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "failedAttempts", constant = "0")
    @Mapping(target = "isLocked", constant = "false")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    UserEntity toEntity(UserCreateRequest request);

    UserResponse toResponse(UserEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "failedAttempts", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget UserEntity entity);
}
