package db.shield.auth.service.service.auth;


import db.shield.auth.service.dto.auth.LoginRequest;
import db.shield.auth.service.dto.auth.LoginResponse;
import db.shield.auth.service.dto.auth.RefreshTokenRequest;
import db.shield.auth.service.dto.auth.RefreshTokenResponse;


public interface AuthService {

    LoginResponse login(LoginRequest loginDto);

    RefreshTokenResponse refreshToken(RefreshTokenRequest refreshDto);
}
