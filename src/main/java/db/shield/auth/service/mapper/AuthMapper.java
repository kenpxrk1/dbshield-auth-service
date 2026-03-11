package db.shield.auth.service.mapper;


import db.shield.auth.service.dto.auth.LoginResponse;
import db.shield.auth.service.dto.auth.RefreshTokenResponse;
import db.shield.auth.service.model.TokenEntity;
import db.shield.auth.service.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "expireTime", ignore = true)
    @Mapping(target = "revoked", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TokenEntity toTokenEntity(UserEntity userEntity, UUID refreshToken);

    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    LoginResponse toLoginResponse(String accessToken, UUID refreshToken);

    RefreshTokenResponse toRefreshResponse(String accessToken, UUID refreshToken);

}
