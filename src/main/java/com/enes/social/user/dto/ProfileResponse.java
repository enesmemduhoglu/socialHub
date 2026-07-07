package com.enes.social.user.dto;

import com.enes.social.user.model.User;

/**
 * Bir kullanıcının herkese açık profili: temel bilgiler + takip sayıları +
 * isteği yapan kullanıcının bu kişiyi takip edip etmediği.
 */
public record ProfileResponse(
        Long id,
        String username,
        String displayName,
        String bio,
        long followerCount,
        long followingCount,
        boolean followedByCurrentUser
) {
    public static ProfileResponse of(User user, long followerCount, long followingCount,
                                     boolean followedByCurrentUser) {
        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                followerCount,
                followingCount,
                followedByCurrentUser
        );
    }
}
