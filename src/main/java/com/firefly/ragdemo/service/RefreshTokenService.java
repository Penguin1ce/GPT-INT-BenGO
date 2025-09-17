package com.firefly.ragdemo.service;

import com.firefly.ragdemo.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String userId, String username);

    Optional<RefreshToken> findValidToken(String token);

    void revokeToken(String token);

    void revokeAllUserTokens(String userId);

    void cleanupExpiredTokens();

    boolean isValidRefreshToken(String token);
}
