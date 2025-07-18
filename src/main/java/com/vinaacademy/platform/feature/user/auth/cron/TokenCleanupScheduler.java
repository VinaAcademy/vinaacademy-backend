package com.vinaacademy.platform.feature.user.auth.cron;

import com.vinaacademy.platform.feature.user.auth.repository.RefreshTokenRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Log4j2
public class TokenCleanupScheduler {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 0/23 * * ?")
//    @Scheduled(cron = "0 * * * * ?")
    public void scheduleRefreshTokenCleanup() {
        log.info("----------start clean up refresh tokens----------");
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        log.info("----------end clean up refresh tokens------------");

    }
}
