package com.firefly.ragdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    private String secret = "mySecretKey";
    private int accessTokenExpiration = 86400; // 24 hours in seconds
    private int refreshTokenExpiration = 604800; // 7 days in seconds

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(int accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public int getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(int refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
