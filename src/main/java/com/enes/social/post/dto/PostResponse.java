package com.enes.social.post.dto;

import com.enes.social.post.model.Post;

import java.time.Instant;

/**
 * Gönderinin dışarıya açılan görünümü.
 */
public record PostResponse(
        Long id,
        String content,
        AuthorSummary author,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getContent(),
                AuthorSummary.from(post.getAuthor()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
