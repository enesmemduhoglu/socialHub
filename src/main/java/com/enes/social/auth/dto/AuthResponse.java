package com.enes.social.auth.dto;

import java.time.Instant;

/**
 * Başarılı kayıt/giriş sonrası dönen JWT ve kullanıcı bilgisi.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, Instant expiresAt, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresAt, user);
    }
}
