package db.shield.auth.service.scheduled;


import db.shield.auth.service.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanup {

    private final TokenRepository tokenRepository;

    @Scheduled(fixedRateString = "${scheduling.fixed-rate}")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired tokens");
        tokenRepository.deleteByExpireTimeIsBefore(OffsetDateTime.now());
    }
}
