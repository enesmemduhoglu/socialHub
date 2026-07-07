package com.enes.social.post.dto;

import com.enes.social.user.model.User;

/**
 * Gönderi içinde gösterilen özet yazar bilgisi.
 */
public record AuthorSummary(
        Long id,
        String username,
        String displayName
) {
    public static AuthorSummary from(User user) {
        return new AuthorSummary(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
