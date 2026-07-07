package com.enes.social.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Mevcut gönderinin içeriğini güncelleme isteği.
 */
public record UpdatePostRequest(

        @NotBlank
        @Size(max = 1000, message = "Gönderi en fazla 1000 karakter olabilir")
        String content
) {
}
