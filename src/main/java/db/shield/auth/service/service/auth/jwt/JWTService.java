package db.shield.auth.service.service.auth.jwt;


import io.jsonwebtoken.Claims;

public interface JWTService {

    String generateAccessToken(String login, String email, String role);
    boolean validateAccessToken(String accessToken);
    Claims getAccessClaims(String token);

}
