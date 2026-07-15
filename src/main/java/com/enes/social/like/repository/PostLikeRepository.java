package com.enes.social.like.repository;

import com.enes.social.like.model.PostLike;
import com.enes.social.post.dto.PostIdCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Beğeni sorguları. Benzersizlik DB kısıtıyla; servis idempotent davranışı sağlar.
 */
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    void deleteByPostIdAndUserId(Long postId, Long userId);

    /** Sayfadaki gönderilerin beğeni sayıları — tek toplu sorgu (N+1 yok). */
    @Query("""
            select pl.post.id as postId, count(pl) as cnt
            from PostLike pl
            where pl.post.id in :postIds
            group by pl.post.id
            """)
    List<PostIdCount> countByPostIds(@Param("postIds") List<Long> postIds);

    /** Kullanıcının, verilen gönderiler içinden beğendiklerinin id'leri. */
    @Query("""
            select pl.post.id
            from PostLike pl
            where pl.user.id = :userId and pl.post.id in :postIds
            """)
    List<Long> findLikedPostIds(@Param("userId") Long userId,
                                @Param("postIds") List<Long> postIds);
}
