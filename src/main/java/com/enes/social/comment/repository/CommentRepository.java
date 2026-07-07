package com.enes.social.comment.repository;

import com.enes.social.comment.model.Comment;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Yorum sorguları. Listeleme keyset (cursor) ile ve yazar join fetch edilerek (N+1 yok).
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByPostId(Long postId);

    @Query("""
            select c from Comment c
            join fetch c.author
            where c.post.id = :postId
              and (:cursor is null or c.id < :cursor)
            order by c.id desc
            """)
    List<Comment> findByPost(@Param("postId") Long postId,
                             @Param("cursor") Long cursor,
                             Limit limit);
}
