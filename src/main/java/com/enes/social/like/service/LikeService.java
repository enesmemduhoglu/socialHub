package com.enes.social.like.service;

import com.enes.social.common.exception.ResourceNotFoundException;
import com.enes.social.like.model.PostLike;
import com.enes.social.like.repository.PostLikeRepository;
import com.enes.social.notification.event.PostLikedEvent;
import com.enes.social.post.repository.PostRepository;
import com.enes.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Beğeni iş mantığı. Hem like hem unlike idempotenttir: tekrar çağrılması durum
 * değiştirmez ve hata üretmez.
 */
@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void like(Long userId, Long postId) {
        requirePostExists(postId);
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return; // zaten beğenilmiş — idempotent
        }
        try {
            likeRepository.save(PostLike.builder()
                    .post(postRepository.getReferenceById(postId))
                    .user(userRepository.getReferenceById(userId))
                    .build());
            likeRepository.flush(); // benzersizlik kısıtını burada tetikle
        } catch (DataIntegrityViolationException e) {
            // Eşzamanlı bir beğeni satırı ekledi; idempotent olarak yok say.
            return;
        }
        // Yalnızca gerçekten yeni bir beğeni oluştuğunda bildirim tetikle.
        eventPublisher.publishEvent(new PostLikedEvent(userId, postId));
    }

    @Transactional
    public void unlike(Long userId, Long postId) {
        requirePostExists(postId);
        likeRepository.deleteByPostIdAndUserId(postId, userId); // yoksa no-op — idempotent
    }

    private void requirePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Gönderi bulunamadı: " + postId);
        }
    }
}
