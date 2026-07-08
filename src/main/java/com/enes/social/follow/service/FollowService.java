package com.enes.social.follow.service;

import com.enes.social.common.config.CacheConfig;
import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.common.exception.BadRequestException;
import com.enes.social.common.exception.DuplicateResourceException;
import com.enes.social.common.exception.ResourceNotFoundException;
import com.enes.social.follow.model.Follow;
import com.enes.social.follow.repository.FollowRepository;
import com.enes.social.notification.event.FollowCreatedEvent;
import com.enes.social.user.dto.ProfileResponse;
import com.enes.social.user.dto.UserSummary;
import com.enes.social.user.model.User;
import com.enes.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Takip iş mantığı: takip et / bırak, takipçi & takip edilen listeleri (keyset),
 * profil sayıları. Benzersizlik ve self-follow kısıtları DB'de de garanti altında.
 */
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.FOLLOWEE_IDS_CACHE, key = "#currentUserId")
    public void follow(Long currentUserId, String targetUsername) {
        User target = findUser(targetUsername);
        if (target.getId().equals(currentUserId)) {
            throw new BadRequestException("Kendinizi takip edemezsiniz");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(currentUserId, target.getId())) {
            throw new DuplicateResourceException("Bu kullanıcıyı zaten takip ediyorsunuz");
        }
        Follow follow = Follow.builder()
                .follower(userRepository.getReferenceById(currentUserId))
                .followee(target)
                .build();
        followRepository.save(follow);
        eventPublisher.publishEvent(new FollowCreatedEvent(currentUserId, target.getId()));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.FOLLOWEE_IDS_CACHE, key = "#currentUserId")
    public void unfollow(Long currentUserId, String targetUsername) {
        User target = findUser(targetUsername);
        Follow follow = followRepository
                .findByFollowerIdAndFolloweeId(currentUserId, target.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bu kullanıcıyı takip etmiyorsunuz"));
        followRepository.delete(follow);
    }

    /**
     * Kullanıcının takip ettiği kişilerin (kendisi dahil) id kümesi. Feed sorgusunun
     * temel girdisi; Redis'te cache'lenir ve follow/unfollow ile geçersiz kılınır.
     */
    @Cacheable(cacheNames = CacheConfig.FOLLOWEE_IDS_CACHE, key = "#userId")
    @Transactional(readOnly = true)
    public List<Long> followeeIdsIncludingSelf(Long userId) {
        List<Long> ids = new ArrayList<>(followRepository.findFolloweeIds(userId));
        ids.add(userId);
        return ids;
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<UserSummary> followers(String username, Long cursor, Integer size) {
        User user = findUser(username);
        int pageSize = CursorPageResponse.normalizeSize(size);
        List<Follow> rows = followRepository.findFollowers(user.getId(), cursor, Limit.of(pageSize + 1));
        return CursorPageResponse.paginate(rows, pageSize,
                f -> UserSummary.from(f.getFollower()), Follow::getId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<UserSummary> following(String username, Long cursor, Integer size) {
        User user = findUser(username);
        int pageSize = CursorPageResponse.normalizeSize(size);
        List<Follow> rows = followRepository.findFollowing(user.getId(), cursor, Limit.of(pageSize + 1));
        return CursorPageResponse.paginate(rows, pageSize,
                f -> UserSummary.from(f.getFollowee()), Follow::getId);
    }

    @Transactional(readOnly = true)
    public ProfileResponse profile(String username, Long currentUserId) {
        User user = findUser(username);
        long followerCount = followRepository.countByFolloweeId(user.getId());
        long followingCount = followRepository.countByFollowerId(user.getId());
        boolean followedByCurrentUser = !user.getId().equals(currentUserId)
                && followRepository.existsByFollowerIdAndFolloweeId(currentUserId, user.getId());
        return ProfileResponse.of(user, followerCount, followingCount, followedByCurrentUser);
    }

    private User findUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
    }
}
