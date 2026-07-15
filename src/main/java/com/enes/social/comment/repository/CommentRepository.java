package com.enes.social.comment.repository;

import com.enes.social.comment.model.Comment;
import com.enes.social.post.dto.PostIdCount;
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

    /** Sayfadaki gönderilerin yorum sayıları — tek toplu sorgu (N+1 yok). */
    @Query("""
            select c.post.id as postId, count(c) as cnt
            from Comment c
            where c.post.id in :postIds
            group by c.post.id
            """)
    List<PostIdCount> countByPostIds(@Param("postIds") List<Long> postIds);

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
