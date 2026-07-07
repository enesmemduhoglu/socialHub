package com.enes.social.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml içindeki app.jwt.* ayarlarının tip güvenli karşılığı.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        String issuer
) {
}
