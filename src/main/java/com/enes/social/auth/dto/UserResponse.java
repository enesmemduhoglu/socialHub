package com.enes.social.auth.dto;

import com.enes.social.user.model.Role;
import com.enes.social.user.model.User;

import java.time.Instant;

/**
 * Kullanıcının dışarıya açılan güvenli görünümü — parola hash'i asla dahil edilmez.
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String bio,
        Role role,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
