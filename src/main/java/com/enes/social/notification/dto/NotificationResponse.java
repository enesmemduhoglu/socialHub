package com.enes.social.notification.dto;

import com.enes.social.notification.model.Notification;
import com.enes.social.notification.model.NotificationType;
import com.enes.social.user.dto.UserSummary;

import java.time.Instant;

/**
 * Bildirimin dışarıya açılan görünümü. postId FOLLOW'da null olabilir.
 */
public record NotificationResponse(
        Long id,
        NotificationType type,
        UserSummary actor,
        Long postId,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                UserSummary.from(n.getActor()),
                n.getPostId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
