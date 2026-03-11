package db.shield.auth.service.service.auth;


import db.shield.auth.service.dto.auth.LoginRequest;
import db.shield.auth.service.dto.auth.LoginResponse;
import db.shield.auth.service.dto.auth.RefreshTokenRequest;
import db.shield.auth.service.dto.auth.RefreshTokenResponse;
import db.shield.auth.service.exception.AuthenticationException;
import db.shield.auth.service.exception.EntityNotFoundException;
import db.shield.auth.service.mapper.AuthMapper;
import db.shield.auth.service.model.TokenEntity;
import db.shield.auth.service.model.UserEntity;
import db.shield.auth.service.repository.TokenRepository;
import db.shield.auth.service.service.auth.jwt.JWTService;
import db.shield.auth.service.service.user.UserService;
import db.shield.auth.service.util.security.details.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${jwt.expire-time-refresh-token}")
    private Long expireTime;
    private final UserService userService;
    private final JWTService jwtService;
    private final TokenRepository tokenRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    @Override
    public LoginResponse login(final LoginRequest loginDto) {
        log.info("Handle authenticate request for user {}", loginDto.username());

        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password()));

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        log.debug("Generating tokens");
        String accessToken = jwtService.generateAccessToken(loginDto.username(), userDetails.getUser().getRole().name());
        UUID refreshToken = generateRefreshToken();
        log.debug("Tokens successfully created");

        TokenEntity token = authMapper.toTokenEntity(userDetails.getUser(), refreshToken);
        token.setExpireTime(OffsetDateTime.now().plusHours(expireTime));

        tokenRepository.save(token);

        return authMapper.toLoginResponse(accessToken, refreshToken);
    }

    @Override
    public RefreshTokenResponse refreshToken(final RefreshTokenRequest refreshDto) {
        log.info("refreshing token");
        TokenEntity tokenToRefresh = tokenRepository.findByRefreshToken(refreshDto.refreshToken()).orElseThrow(() -> new EntityNotFoundException("Token '" + refreshDto.refreshToken() + "' is invalid"));

        if (tokenToRefresh.isRevoked() || tokenToRefresh.getExpireTime().isBefore(OffsetDateTime.now())) {
            throw new AuthenticationException("Refresh token expired or revoked");
        }

        UserEntity user = tokenToRefresh.getUser();
        UUID refreshToken = generateRefreshToken();
        tokenToRefresh.setRefreshToken(refreshToken);
        tokenToRefresh.setExpireTime(OffsetDateTime.now().plusHours(expireTime));
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole().name());
        return authMapper.toRefreshResponse(accessToken, refreshToken);
    }

    private UUID generateRefreshToken() {
        return UUID.randomUUID();
    }
}
