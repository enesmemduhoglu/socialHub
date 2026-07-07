package com.enes.social.follow.service;

import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.common.exception.BadRequestException;
import com.enes.social.common.exception.DuplicateResourceException;
import com.enes.social.common.exception.ResourceNotFoundException;
import com.enes.social.follow.model.Follow;
import com.enes.social.follow.repository.FollowRepository;
import com.enes.social.user.dto.ProfileResponse;
import com.enes.social.user.dto.UserSummary;
import com.enes.social.user.model.User;
import com.enes.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

/**
 * Takip iş mantığı: takip et / bırak, takipçi & takip edilen listeleri (keyset),
 * profil sayıları. Benzersizlik ve self-follow kısıtları DB'de de garanti altında.
 */
@Service
@RequiredArgsConstructor
public class FollowService {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 50;

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
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
    }

    @Transactional
    public void unfollow(Long currentUserId, String targetUsername) {
        User target = findUser(targetUsername);
        Follow follow = followRepository
                .findByFollowerIdAndFolloweeId(currentUserId, target.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bu kullanıcıyı takip etmiyorsunuz"));
        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<UserSummary> followers(String username, Long cursor, Integer size) {
        User user = findUser(username);
        int pageSize = normalizeSize(size);
        List<Follow> rows = followRepository.findFollowers(user.getId(), cursor, Limit.of(pageSize + 1));
        return toPage(rows, pageSize, f -> UserSummary.from(f.getFollower()));
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<UserSummary> following(String username, Long cursor, Integer size) {
        User user = findUser(username);
        int pageSize = normalizeSize(size);
        List<Follow> rows = followRepository.findFollowing(user.getId(), cursor, Limit.of(pageSize + 1));
        return toPage(rows, pageSize, f -> UserSummary.from(f.getFollowee()));
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

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private CursorPageResponse<UserSummary> toPage(List<Follow> rows, int pageSize,
                                                  Function<Follow, UserSummary> mapper) {
        boolean hasMore = rows.size() > pageSize;
        List<Follow> page = hasMore ? rows.subList(0, pageSize) : rows;
        List<UserSummary> items = page.stream().map(mapper).toList();
        Long nextCursor = hasMore ? page.get(page.size() - 1).getId() : null;
        return CursorPageResponse.of(items, nextCursor, hasMore);
    }
}
