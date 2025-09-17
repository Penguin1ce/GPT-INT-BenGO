package com.firefly.ragdemo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    private String id;

    private String userId;

    private String token;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    @Builder.Default
    private Boolean isRevoked = false;
}
