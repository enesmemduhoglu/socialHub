package com.enes.social.notification.event;

/**
 * actorId, postId gönderisini beğendi. Alıcı (gönderi sahibi) dinleyicide çözülür.
 */
public record PostLikedEvent(Long actorId, Long postId) {
}
