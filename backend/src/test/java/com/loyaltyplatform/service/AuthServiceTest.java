package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.LoginRequest;
import com.loyaltyplatform.dto.request.RegisterRequest;
import com.loyaltyplatform.dto.response.AuthResponse;
import com.loyaltyplatform.entity.User;
import com.loyaltyplatform.enums.Role;
import com.loyaltyplatform.exception.DuplicateResourceException;
import com.loyaltyplatform.repository.UserRepository;
import com.loyaltyplatform.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("Password1!")
                .role(Role.ROLE_MARKETING_MANAGER)
                .build();

        loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("Password1!")
                .build();

        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encoded_password")
                .role(Role.ROLE_MARKETING_MANAGER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterNewUser() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(
                com.loyaltyplatform.entity.RefreshToken.builder()
                        .token("refresh_token")
                        .user(testUser)
                        .expiryDate(java.time.Instant.now().plusSeconds(86400))
                        .build());
        doNothing().when(auditLogService).logByEmail(any(), any(), any(), any(), any());

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_MARKETING_MANAGER);
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for existing email")
    void shouldThrowExceptionForDuplicateEmail() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(any())).thenReturn("access_token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(
                com.loyaltyplatform.entity.RefreshToken.builder()
                        .token("refresh_token")
                        .user(testUser)
                        .expiryDate(java.time.Instant.now().plusSeconds(86400))
                        .build());
        doNothing().when(auditLogService).log(any(), any(), any(), any(), any());

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for invalid credentials")
    void shouldThrowBadCredentialsForInvalidLogin() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}
