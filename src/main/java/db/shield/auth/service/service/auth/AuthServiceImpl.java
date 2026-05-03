package db.shield.auth.service.service.auth;


import db.shield.auth.lib.security.JWTService;
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
import db.shield.auth.service.repository.UserRepository;
import db.shield.auth.service.util.security.details.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${jwt.expire-time-refresh-token}")
    private Long expireTime;
    private final JWTService jwtService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final AuthenticationManager authManager;

    @Transactional
    @Override
    public LoginResponse login(final LoginRequest loginDto) {
        log.info("Handle authenticate request for user {}", loginDto.username());

        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password()));

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        UserEntity user = userDetails.getUser();
        user.setLastLoginAt(OffsetDateTime.now());
        user.setFailedAttempts(0);

        log.debug("Generating tokens");
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getEmail(), user.getRole().name());
        UUID refreshToken = generateRefreshToken();
        log.debug("Tokens successfully created");

        userRepository.save(user);
        TokenEntity token = authMapper.toTokenEntity(user, refreshToken);
        token.setExpireTime(OffsetDateTime.now().plusHours(expireTime));

        tokenRepository.save(token);

        return authMapper.toLoginResponse(accessToken, refreshToken);
    }

    @Transactional
    @Override
    public RefreshTokenResponse refreshToken(final RefreshTokenRequest refreshDto) {
        log.info("refreshing token");
        TokenEntity tokenToRefresh = tokenRepository.findByRefreshToken(refreshDto.refreshToken()).orElseThrow(
                () -> new EntityNotFoundException("Token '" + refreshDto.refreshToken() + "' is invalid"));

        if (tokenToRefresh.isRevoked() || tokenToRefresh.getExpireTime().isBefore(OffsetDateTime.now())) {
            throw new AuthenticationException("Refresh token expired or revoked");
        }

        UserEntity user = tokenToRefresh.getUser();
        UUID refreshToken = generateRefreshToken();
        tokenToRefresh.setRefreshToken(refreshToken);
        tokenToRefresh.setExpireTime(OffsetDateTime.now().plusHours(expireTime));
        tokenRepository.save(tokenToRefresh);
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getEmail(), user.getRole().name());
        return authMapper.toRefreshResponse(accessToken, refreshToken);
    }

    private UUID generateRefreshToken() {
        return UUID.randomUUID();
    }
}
