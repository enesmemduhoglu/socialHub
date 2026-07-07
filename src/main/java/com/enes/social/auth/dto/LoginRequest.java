package com.enes.social.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Giriş isteği. Kullanıcı hem username hem email ile giriş yapabilir.
 */
public record LoginRequest(

        @NotBlank
        String usernameOrEmail,

        @NotBlank
        String password
) {
}
