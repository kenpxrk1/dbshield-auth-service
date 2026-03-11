package db.shield.auth.service.service.auth.jwt;


public interface JWTService {

    String generateAccessToken(String login, String role);
}
