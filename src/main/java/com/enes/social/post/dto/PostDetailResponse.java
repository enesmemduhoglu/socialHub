package com.enes.social.post.dto;

import com.enes.social.post.model.Post;

import java.time.Instant;

/**
 * Tek gönderi detayının görünümü: temel alanlar + beğeni/yorum sayıları +
 * isteği yapan kullanıcının bu gönderiyi beğenip beğenmediği.
 *
 * <p>Liste/feed yanıtları {@link PostResponse} kullanır (sayılar dahil edilmez),
 * böylece toplu sorgularda gereksiz N+1 sayımlardan kaçınılır.
 */
public record PostDetailResponse(
        Long id,
        String content,
        AuthorSummary author,
        Instant createdAt,
        Instant updatedAt,
        long likeCount,
        long commentCount,
        boolean likedByCurrentUser
) {
    public static PostDetailResponse from(Post post, long likeCount, long commentCount,
                                          boolean likedByCurrentUser) {
        return new PostDetailResponse(
                post.getId(),
                post.getContent(),
                AuthorSummary.from(post.getAuthor()),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likeCount,
                commentCount,
                likedByCurrentUser
        );
    }
}
