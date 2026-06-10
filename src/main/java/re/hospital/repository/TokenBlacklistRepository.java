package re.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import re.hospital.model.entity.TokenBlacklist;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByToken(String token);
}
