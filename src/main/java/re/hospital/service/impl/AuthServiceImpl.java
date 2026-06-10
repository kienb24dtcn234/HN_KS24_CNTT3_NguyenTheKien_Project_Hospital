package re.hospital.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import re.hospital.exception.ConflictException;
import re.hospital.model.dto.request.LoginRequest;
import re.hospital.model.dto.request.RegisterRequest;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.entity.*;
import re.hospital.model.enums.RoleName;
import re.hospital.repository.*;
import re.hospital.security.jwt.JWTProvider;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.AuthService;
import re.hospital.service.RefreshTokenService;
import re.hospital.model.dto.request.ChangePasswordRequest;
import re.hospital.model.dto.request.ForgotPasswordRequest;
import re.hospital.exception.ResourceNotFoundException;


import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    @Transactional
    public JWTResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser().getId());

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        return JWTResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .fullName(userDetails.getUser().getFullName())
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ConflictException("Username already exists");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already exists");

        Role patientRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                .orElseThrow(() -> new RuntimeException("Role PATIENT not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .active(true)
                .roles(Set.of(patientRole))
                .build();

        userRepository.save(user);
        return "Registration successful";
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        tokenBlacklistRepository.save(TokenBlacklist.builder()
                .token(accessToken)
                .expiryDate(jwtProvider.getExpirationFromToken(accessToken).toInstant())
                .build());
    }

    @Override
    @Transactional
    public String changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ConflictException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password changed successfully";
    }

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password reset successfully";
    }

}
