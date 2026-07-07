package com.enes.social.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Yeni kullanıcı kaydı isteği.
 */
public record RegisterRequest(

        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$",
                message = "Kullanıcı adı yalnızca harf, rakam ve alt çizgi içerebilir")
        String username,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 72, message = "Parola 8-72 karakter olmalıdır")
        String password,

        @Size(max = 60)
        String displayName
) {
}
