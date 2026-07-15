package com.enes.social.post.dto;

import com.enes.social.post.model.Post;

import java.time.Instant;

/**
 * Gönderinin dışarıya açılan görünümü. Liste ve detay uçları aynı şekli döner;
 * beğeni/yorum sayıları listelerde toplu sorguyla (N+1 olmadan) doldurulur.
 */
public record PostResponse(
        Long id,
        String content,
        AuthorSummary author,
        Instant createdAt,
        Instant updatedAt,
        long likeCount,
        long commentCount,
        boolean likedByCurrentUser
) {
    public static PostResponse from(Post post, long likeCount, long commentCount,
                                    boolean likedByCurrentUser) {
        return new PostResponse(
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

    /** Yeni oluşturulan gönderi: henüz beğeni ve yorum olamaz. */
    public static PostResponse fresh(Post post) {
        return from(post, 0, 0, false);
    }
}
