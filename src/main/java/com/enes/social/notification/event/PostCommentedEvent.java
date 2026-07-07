package com.enes.social.notification.event;

/**
 * actorId, postId gönderisine yorum yaptı. Alıcı (gönderi sahibi) dinleyicide çözülür.
 */
public record PostCommentedEvent(Long actorId, Long postId) {
}
