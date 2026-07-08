package com.enes.social.notification.service;

import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.common.exception.ResourceNotFoundException;
import com.enes.social.notification.dto.NotificationResponse;
import com.enes.social.notification.model.Notification;
import com.enes.social.notification.model.NotificationType;
import com.enes.social.notification.repository.NotificationRepository;
import com.enes.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bildirim iş mantığı: üretim (self-eylemde üretilmez), keyset listeleme,
 * okunmamış sayısı ve okundu işaretleme.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(Long recipientId, Long actorId, NotificationType type, Long postId) {
        if (recipientId.equals(actorId)) {
            return; // Kendi eylemine bildirim üretme.
        }
        Notification notification = Notification.builder()
                .recipient(userRepository.getReferenceById(recipientId))
                .actor(userRepository.getReferenceById(actorId))
                .type(type)
                .postId(postId)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationResponse> list(Long userId, Long cursor, Integer size) {
        int pageSize = CursorPageResponse.normalizeSize(size);
        List<Notification> rows = notificationRepository.findForRecipient(userId, cursor, Limit.of(pageSize + 1));
        return CursorPageResponse.paginate(rows, pageSize, NotificationResponse::from, Notification::getId);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bildirim bulunamadı: " + id));
        notification.setRead(true); // dirty checking commit'te UPDATE yazar
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }
}
