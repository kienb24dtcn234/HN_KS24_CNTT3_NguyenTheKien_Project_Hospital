package re.hospital.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import re.hospital.exception.TokenRefreshException;
import re.hospital.model.dto.request.LoginRequest;
import re.hospital.model.dto.request.RefreshTokenRequest;
import re.hospital.model.dto.request.RegisterRequest;
import re.hospital.model.dto.response.ApiResponse;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.dto.response.TokenRefreshResponse;
import re.hospital.model.entity.RefreshToken;
import re.hospital.repository.RefreshTokenRepository;
import re.hospital.security.jwt.JWTProvider;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.AuthService;
import re.hospital.service.RefreshTokenService;
import re.hospital.model.dto.request.ChangePasswordRequest;
import re.hospital.model.dto.request.ForgotPasswordRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import re.hospital.model.dto.request.ResetPasswordRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JWTResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        refreshTokenService.verifyExpiration(refreshToken);

        String newAccessToken = jwtProvider.generateAccessToken(new CustomUserDetails(refreshToken.getUser()));

        return ResponseEntity.ok(ApiResponse.success("Token refreshed", TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .type("Bearer").build()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String bearerToken) {
        String accessToken = bearerToken.substring(7);
        authService.logout(accessToken);

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        refreshTokenService.revokeAllUserTokens(userDetails.getUser().getId());

        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.changePassword(userDetails.getUser().getId(), request)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.forgotPassword(request)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.resetPassword(request)));
    }


}
