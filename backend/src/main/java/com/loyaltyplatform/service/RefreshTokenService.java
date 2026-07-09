package com.loyaltyplatform.service;

import com.loyaltyplatform.entity.RefreshToken;
import com.loyaltyplatform.entity.User;
import com.loyaltyplatform.exception.BadRequestException;
import com.loyaltyplatform.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        revokeAllTokens(user);
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateAndGetToken(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (token.isRevoked()) {
            throw new BadRequestException("Refresh token has been revoked");
        }
        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token has expired");
        }
        return token;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        return createRefreshToken(oldToken.getUser());
    }

    @Transactional
    public void revokeAllTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }
}
