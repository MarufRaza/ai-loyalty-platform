package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.*;
import com.loyaltyplatform.dto.response.AuthResponse;
import com.loyaltyplatform.entity.RefreshToken;
import com.loyaltyplatform.entity.User;
import com.loyaltyplatform.enums.AuditAction;
import com.loyaltyplatform.exception.BadRequestException;
import com.loyaltyplatform.exception.DuplicateResourceException;
import com.loyaltyplatform.repository.UserRepository;
import com.loyaltyplatform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {} with role {}", savedUser.getEmail(), savedUser.getRole());

        auditLogService.log(savedUser, AuditAction.USER_REGISTER,
                "User", savedUser.getId(), "New user registered: " + savedUser.getEmail());

        return buildAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        auditLogService.log(user, AuditAction.USER_LOGIN,
                "User", user.getId(), "User logged in");

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateAndGetToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        String newAccessToken = tokenProvider.generateTokenFromUsername(
                user.getEmail(), user.getRole().name());
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        refreshTokenService.revokeAllTokens(user);
        auditLogService.log(user, AuditAction.USER_LOGOUT,
                "User", user.getId(), "User logged out");
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = tokenProvider.generateTokenFromUsername(
                user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .issuedAt(LocalDateTime.now())
                .build();
    }
}
