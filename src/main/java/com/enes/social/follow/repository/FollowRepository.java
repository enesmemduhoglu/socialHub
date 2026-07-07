package com.enes.social.follow.repository;

import com.enes.social.follow.model.Follow;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Takip sorguları. Listeler keyset (cursor) sayfalaması ile ve karşı tarafı
 * join fetch ederek N+1'i önler.
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    /** userId'nin takip ettiği kişilerin id'leri (feed için). */
    @Query("select f.followee.id from Follow f where f.follower.id = :userId")
    List<Long> findFolloweeIds(@Param("userId") Long userId);

    /** userId'nin takipçi sayısı. */
    long countByFolloweeId(Long followeeId);

    /** userId'nin takip ettiği kişi sayısı. */
    long countByFollowerId(Long followerId);

    /** userId'yi takip edenler (takipçiler), en yeniden eskiye keyset. */
    @Query("""
            select f from Follow f
            join fetch f.follower
            where f.followee.id = :userId
              and (:cursor is null or f.id < :cursor)
            order by f.id desc
            """)
    List<Follow> findFollowers(@Param("userId") Long userId,
                               @Param("cursor") Long cursor,
                               Limit limit);

    /** userId'nin takip ettikleri, en yeniden eskiye keyset. */
    @Query("""
            select f from Follow f
            join fetch f.followee
            where f.follower.id = :userId
              and (:cursor is null or f.id < :cursor)
            order by f.id desc
            """)
    List<Follow> findFollowing(@Param("userId") Long userId,
                               @Param("cursor") Long cursor,
                               Limit limit);
}
