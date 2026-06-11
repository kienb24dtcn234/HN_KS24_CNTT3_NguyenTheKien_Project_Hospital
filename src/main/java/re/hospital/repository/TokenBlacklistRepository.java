package re.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import re.hospital.model.entity.TokenBlacklist;
import java.time.Instant;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByToken(String token);
    long deleteByExpiryDateBefore(Instant date);
}
