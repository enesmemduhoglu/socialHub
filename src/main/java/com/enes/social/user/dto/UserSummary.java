package com.enes.social.user.dto;

import com.enes.social.user.model.User;

/**
 * Listeler (takipçi/takip edilen vb.) için kompakt kullanıcı görünümü.
 */
public record UserSummary(
        Long id,
        String username,
        String displayName
) {
    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
