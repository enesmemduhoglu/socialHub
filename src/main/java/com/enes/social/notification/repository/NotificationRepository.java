package com.enes.social.notification.repository;

import com.enes.social.notification.model.Notification;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Bildirim sorguları. Liste keyset (cursor) ile ve actor join fetch edilerek (N+1 yok).
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            select n from Notification n
            join fetch n.actor
            where n.recipient.id = :userId
              and (:cursor is null or n.id < :cursor)
            order by n.id desc
            """)
    List<Notification> findForRecipient(@Param("userId") Long userId,
                                        @Param("cursor") Long cursor,
                                        Limit limit);

    long countByRecipientIdAndReadFalse(Long recipientId);

    /** Sahiplik güvenli erişim: yalnızca alıcısına ait bildirimi döner. */
    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    @Modifying
    @Query("update Notification n set n.read = true where n.recipient.id = :userId and n.read = false")
    int markAllRead(@Param("userId") Long userId);
}
