package re.hospital.service;

import re.hospital.model.dto.request.*;
import re.hospital.model.dto.response.JWTResponse;

public interface AuthService {
    JWTResponse login(LoginRequest request);
    String register(RegisterRequest request);
    void logout(String accessToken);
    String changePassword(Long userId, ChangePasswordRequest request);
    String forgotPassword(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordRequest request);
}
