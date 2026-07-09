package com.enes.social.user.dto;

import jakarta.validation.constraints.Size;

/**
 * Profil güncelleme isteği (PATCH semantiği): null bırakılan alan değiştirilmez,
 * boş string gönderilen alan temizlenir (null'a çekilir).
 */
public record UpdateProfileRequest(

        @Size(max = 60, message = "Görünen ad en fazla 60 karakter olabilir")
        String displayName,

        @Size(max = 160, message = "Bio en fazla 160 karakter olabilir")
        String bio
) {
}
