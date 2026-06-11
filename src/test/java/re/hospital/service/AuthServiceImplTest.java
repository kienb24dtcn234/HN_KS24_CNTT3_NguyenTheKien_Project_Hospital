package re.hospital.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import re.hospital.exception.ConflictException;
import re.hospital.model.dto.request.LoginRequest;
import re.hospital.model.dto.request.RegisterRequest;
import re.hospital.model.dto.response.JWTResponse;
import re.hospital.model.entity.RefreshToken;
import re.hospital.model.entity.Role;
import re.hospital.model.entity.User;
import re.hospital.model.enums.RoleName;
import re.hospital.repository.RoleRepository;
import re.hospital.repository.TokenBlacklistRepository;
import re.hospital.repository.UserRepository;
import re.hospital.security.jwt.JWTProvider;
import re.hospital.security.principal.CustomUserDetails;
import re.hospital.service.impl.AuthServiceImpl;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JWTProvider jwtProvider;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role patientRole;

    @BeforeEach
    void setUp() {
        patientRole = Role.builder().id(1L).name(RoleName.ROLE_PATIENT).build();
        testUser = User.builder()
                .id(1L).username("testuser").email("test@gmail.com")
                .password("encoded").fullName("Test User").active(true)
                .roles(Set.of(patientRole)).build();
    }

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtProvider.generateAccessToken(any())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(1L))
                .thenReturn(RefreshToken.builder().token("refresh-token")
                        .expiryDate(Instant.now().plusSeconds(3600)).build());

        JWTResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("testuser", response.getUsername());
        assertTrue(response.getRoles().contains("ROLE_PATIENT"));
    }

    @Test
    @DisplayName("Login - Bad Credentials")
    void login_BadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Register - Success")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@gmail.com");
        request.setPassword("123456");
        request.setFullName("New User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@gmail.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_PATIENT)).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode("123456")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(testUser);

        String result = authService.register(request);

        assertEquals("Registration successful", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Username Exists")
    void register_UsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setEmail("new@gmail.com");
        request.setPassword("123456");
        request.setFullName("Test");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register - Email Exists")
    void register_EmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@gmail.com");
        request.setPassword("123456");
        request.setFullName("Test");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@gmail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }
}
