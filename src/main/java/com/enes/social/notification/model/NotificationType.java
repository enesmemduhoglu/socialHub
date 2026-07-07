package com.enes.social.notification.model;

/**
 * Bildirim türleri. FOLLOW'da post yoktur; LIKE/COMMENT bir gönderiye bağlıdır.
 */
public enum NotificationType {
    FOLLOW,
    LIKE,
    COMMENT
}
