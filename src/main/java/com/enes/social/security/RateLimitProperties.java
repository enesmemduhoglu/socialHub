package com.enes.social.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * application.yml içindeki app.rate-limit.* ayarlarının tip güvenli karşılığı.
 * capacity: pencere başına izin verilen istek sayısı; refillPeriod: pencerenin süresi.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        int capacity,
        Duration refillPeriod
) {
}
