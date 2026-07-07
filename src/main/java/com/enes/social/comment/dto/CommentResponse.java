package com.enes.social.comment.dto;

import com.enes.social.comment.model.Comment;
import com.enes.social.post.dto.AuthorSummary;

import java.time.Instant;

/**
 * Yorumun dışarıya açılan görünümü.
 */
public record CommentResponse(
        Long id,
        String content,
        AuthorSummary author,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                AuthorSummary.from(comment.getAuthor()),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
