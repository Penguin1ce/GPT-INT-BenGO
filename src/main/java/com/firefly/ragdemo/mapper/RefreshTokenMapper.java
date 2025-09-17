package com.firefly.ragdemo.mapper;

import com.firefly.ragdemo.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {

    Optional<RefreshToken> findByToken(@Param("token") String token);

    Optional<RefreshToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    int insert(RefreshToken token);

    int revokeAllTokensByUserId(@Param("userId") String userId);

    int revokeToken(@Param("token") String token);

    int deleteExpiredTokens(@Param("now") LocalDateTime now);
} 