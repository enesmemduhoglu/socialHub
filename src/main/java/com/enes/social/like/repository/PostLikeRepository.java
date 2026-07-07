package com.enes.social.like.repository;

import com.enes.social.like.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Beğeni sorguları. Benzersizlik DB kısıtıyla; servis idempotent davranışı sağlar.
 */
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    void deleteByPostIdAndUserId(Long postId, Long userId);
}
