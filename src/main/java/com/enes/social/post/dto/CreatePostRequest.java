package com.enes.social.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Yeni gönderi oluşturma isteği.
 */
public record CreatePostRequest(

        @NotBlank
        @Size(max = 1000, message = "Gönderi en fazla 1000 karakter olabilir")
        String content
) {
}
