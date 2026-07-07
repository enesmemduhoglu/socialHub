package com.enes.social.notification.event;

/**
 * actorId, recipientId'yi takip etti. Bildirim üretimi bu olayı dinler.
 */
public record FollowCreatedEvent(Long actorId, Long recipientId) {
}
