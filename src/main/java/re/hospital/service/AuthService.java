package re.hospital.service;

import re.hospital.model.dto.request.LoginRequest;
import re.hospital.model.dto.request.RegisterRequest;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.dto.request.ChangePasswordRequest;
import re.hospital.model.dto.request.ForgotPasswordRequest;


public interface AuthService {
    JWTResponse login(LoginRequest request);
    String register(RegisterRequest request);
    void logout(String accessToken);
    String changePassword(Long userId, ChangePasswordRequest request);
    String forgotPassword(ForgotPasswordRequest request);

}
