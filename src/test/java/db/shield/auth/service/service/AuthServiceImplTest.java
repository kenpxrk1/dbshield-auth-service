package db.shield.auth.service.service;


import db.shield.auth.service.Initializer;
import db.shield.auth.service.dto.auth.LoginRequest;
import db.shield.auth.service.dto.auth.LoginResponse;
import db.shield.auth.service.dto.auth.RefreshTokenRequest;
import db.shield.auth.service.dto.auth.RefreshTokenResponse;
import db.shield.auth.service.exception.AuthenticationException;
import db.shield.auth.service.exception.EntityNotFoundException;
import db.shield.auth.service.mapper.AuthMapper;
import db.shield.auth.service.mapper.AuthMapperImpl;
import db.shield.auth.service.model.TokenEntity;
import db.shield.auth.service.repository.TokenRepository;
import db.shield.auth.service.repository.UserRepository;
import db.shield.auth.service.service.auth.AuthServiceImpl;
import db.shield.auth.service.service.auth.jwt.JWTService;
import db.shield.auth.service.util.security.details.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest extends Initializer {

    @Mock
    private AuthenticationManager authManager;
    @Mock
    private JWTService jwtService;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private AuthMapper authMapper = new AuthMapperImpl();
    @InjectMocks
    private AuthServiceImpl authService;
    private static final String PASSWORD = "password123";
    private static final String ACCESS_TOKEN = "access-token";
    private static final UUID REFRESH_TOKEN = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expireTime", 1L);
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        CustomUserDetails userDetails = new CustomUserDetails(userEntity);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null);

        when(authManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn(ACCESS_TOKEN);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginResponse response = authService.login(request);

        verify(authManager).authenticate(any());
        verify(jwtService).generateAccessToken(USERNAME, userEntity.getEmail(), userEntity.getRole().name());
        verify(userRepository).save(any());
        verify(tokenRepository).save(any(TokenEntity.class));

        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.accessToken());
        assertNotNull(response.refreshToken());
    }

    @Test
    void refreshToken_success() {
        UUID existingToken = UUID.randomUUID();

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setRefreshToken(existingToken);
        tokenEntity.setExpireTime(OffsetDateTime.now().plusHours(1));
        tokenEntity.setRevoked(false);
        tokenEntity.setUser(userEntity);

        when(tokenRepository.findByRefreshToken(existingToken))
                .thenReturn(Optional.of(tokenEntity));

        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any(), any(), any()))
                .thenReturn(ACCESS_TOKEN);

        RefreshTokenRequest request = new RefreshTokenRequest(existingToken);

        RefreshTokenResponse response = authService.refreshToken(request);

        verify(tokenRepository).findByRefreshToken(existingToken);
        verify(tokenRepository).save(any(TokenEntity.class));
        verify(jwtService).generateAccessToken(USERNAME, userEntity.getEmail(), userEntity.getRole().name());

        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.accessToken());
        assertNotNull(response.refreshToken());
        assertNotEquals(existingToken, response.refreshToken());
    }

    @Test
    void refreshToken_notFound() {
        UUID token = UUID.randomUUID();

        when(tokenRepository.findByRefreshToken(token))
                .thenReturn(Optional.empty());

        RefreshTokenRequest request = new RefreshTokenRequest(token);

        assertThrows(EntityNotFoundException.class,
                () -> authService.refreshToken(request));
    }

    @Test
    void refreshToken_expired() {
        UUID token = UUID.randomUUID();

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setRefreshToken(token);
        tokenEntity.setExpireTime(OffsetDateTime.now().minusHours(1));
        tokenEntity.setRevoked(false);
        tokenEntity.setUser(userEntity);

        when(tokenRepository.findByRefreshToken(token))
                .thenReturn(Optional.of(tokenEntity));

        RefreshTokenRequest request = new RefreshTokenRequest(token);

        assertThrows(AuthenticationException.class,
                () -> authService.refreshToken(request));
    }

    @Test
    void refreshToken_revoked() {
        UUID token = UUID.randomUUID();

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setRefreshToken(token);
        tokenEntity.setExpireTime(OffsetDateTime.now().plusHours(1));
        tokenEntity.setRevoked(true);
        tokenEntity.setUser(userEntity);

        when(tokenRepository.findByRefreshToken(token))
                .thenReturn(Optional.of(tokenEntity));

        RefreshTokenRequest request = new RefreshTokenRequest(token);

        assertThrows(AuthenticationException.class,
                () -> authService.refreshToken(request));
    }
}
