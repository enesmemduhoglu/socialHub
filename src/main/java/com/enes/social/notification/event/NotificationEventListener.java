package com.enes.social.notification.event;

import com.enes.social.notification.model.NotificationType;
import com.enes.social.notification.service.NotificationService;
import com.enes.social.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Domain olaylarını dinleyip bildirim üretir. Dinleyiciler ilgili işlemin
 * transaction'ı COMMIT olduktan SONRA çalışır ({@link TransactionPhase#AFTER_COMMIT}) —
 * böylece bildirim yalnızca eylem gerçekten kalıcı olduysa oluşur ve çekirdek akıştan ayrışır.
 *
 * <p>AFTER_COMMIT fazında orijinal transaction tamamlanmıştır; yazma yapabilmek için
 * {@link Propagation#REQUIRES_NEW} ile yeni bir transaction açılır.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final PostRepository postRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onFollow(FollowCreatedEvent event) {
        notificationService.create(event.recipientId(), event.actorId(), NotificationType.FOLLOW, null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLike(PostLikedEvent event) {
        postRepository.findAuthorId(event.postId()).ifPresent(authorId ->
                notificationService.create(authorId, event.actorId(), NotificationType.LIKE, event.postId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onComment(PostCommentedEvent event) {
        postRepository.findAuthorId(event.postId()).ifPresent(authorId ->
                notificationService.create(authorId, event.actorId(), NotificationType.COMMENT, event.postId()));
    }
}
