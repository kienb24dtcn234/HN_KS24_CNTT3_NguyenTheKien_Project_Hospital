package re.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import re.hospital.model.entity.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByEmailAndOtpAndUsedFalse(String email, String otp);
    long deleteByExpiryDateBefore(Instant date);
}
