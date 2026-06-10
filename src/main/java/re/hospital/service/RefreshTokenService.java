package re.hospital.service;

import re.hospital.model.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    RefreshToken verifyExpiration(RefreshToken token);
    void revokeAllUserTokens(Long userId);
}
