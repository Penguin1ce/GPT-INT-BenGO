package com.firefly.ragdemo.service;

import com.firefly.ragdemo.entity.RefreshToken;
import com.firefly.ragdemo.entity.User;
import com.firefly.ragdemo.mapper.RefreshTokenRepository;
import com.firefly.ragdemo.secutiry.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        String tokenValue = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7天过期
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidToken(String token) {
        return refreshTokenRepository.findValidToken(token, LocalDateTime.now());
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.revokeToken(token);
    }

    @Transactional
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.revokeAllTokensByUserId(userId);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired refresh tokens");
    }

    public boolean isValidRefreshToken(String token) {
        try {
            return jwtUtil.validateToken(token) &&
                    findValidToken(token).isPresent();
        } catch (Exception e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }
}
