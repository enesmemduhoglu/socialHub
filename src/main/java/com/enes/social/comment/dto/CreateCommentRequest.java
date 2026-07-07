package com.enes.social.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Yeni yorum oluşturma isteği.
 */
public record CreateCommentRequest(

        @NotBlank
        @Size(max = 500, message = "Yorum en fazla 500 karakter olabilir")
        String content
) {
}
