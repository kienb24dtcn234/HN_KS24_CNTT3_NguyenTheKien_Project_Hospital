package re.hospital.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation. Transactional;
import re.hospital.repository.TokenBlacklistRepository;
import re.hospital.repository.PasswordResetTokenRepository;

import java.time.Instant;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        long deletedBlacklist = tokenBlacklistRepository.deleteByExpiryDateBefore(Instant.now());
        long deletedResetTokens = passwordResetTokenRepository.deleteByExpiryDateBefore(Instant.now());
        log.info("[CLEANUP] Removed {} expired blacklist tokens, {} expired reset tokens",
                deletedBlacklist, deletedResetTokens);
    }
}
