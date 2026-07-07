package com.enes.social.post.repository;

import com.enes.social.post.model.Post;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Gönderi sorguları. Listeleme, keyset (cursor) sayfalaması ile ve yazarı
 * join fetch ederek N+1 sorununu önler.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /** Tek gönderiyi yazarıyla birlikte getirir (detay görünümü için). */
    @Query("select p from Post p join fetch p.author where p.id = :id")
    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

    /**
     * Genel akış: id'ye göre azalan keyset sayfası.
     * cursor null ise en baştan başlar; değilse id < cursor olanları getirir.
     */
    @Query("""
            select p from Post p
            join fetch p.author
            where (:cursor is null or p.id < :cursor)
            order by p.id desc
            """)
    List<Post> findFeed(@Param("cursor") Long cursor, Limit limit);

    /** Belirli bir yazarın zaman tüneli, aynı keyset mantığıyla. */
    @Query("""
            select p from Post p
            join fetch p.author
            where p.author.id = :authorId
              and (:cursor is null or p.id < :cursor)
            order by p.id desc
            """)
    List<Post> findByAuthor(@Param("authorId") Long authorId,
                            @Param("cursor") Long cursor,
                            Limit limit);

    /**
     * Kişisel akış (home timeline): verilen yazar kümesinin gönderileri, keyset.
     * Yazar join fetch edilerek N+1 önlenir.
     */
    @Query("""
            select p from Post p
            join fetch p.author
            where p.author.id in :authorIds
              and (:cursor is null or p.id < :cursor)
            order by p.id desc
            """)
    List<Post> findTimeline(@Param("authorIds") Collection<Long> authorIds,
                            @Param("cursor") Long cursor,
                            Limit limit);
}
