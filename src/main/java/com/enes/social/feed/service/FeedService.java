package com.enes.social.feed.service;

import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.follow.service.FollowService;
import com.enes.social.post.dto.PostResponse;
import com.enes.social.post.model.Post;
import com.enes.social.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kişisel akış (home timeline): kullanıcının kendi ve takip ettiği kişilerin
 * gönderileri, en yeniden eskiye keyset sayfalama ile.
 *
 * <p>Takip edilenlerin id kümesi {@link FollowService} üzerinden Redis'ten cache'li
 * gelir; gönderiler tek sorguda yazarları join fetch edilerek çekilir (N+1 yok).
 */
@Service
@RequiredArgsConstructor
public class FeedService {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 50;

    private final FollowService followService;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> timeline(Long userId, Long cursor, Integer size) {
        List<Long> authorIds = followService.followeeIdsIncludingSelf(userId);
        int pageSize = normalizeSize(size);

        List<Post> rows = postRepository.findTimeline(authorIds, cursor, Limit.of(pageSize + 1));
        boolean hasMore = rows.size() > pageSize;
        List<Post> page = hasMore ? rows.subList(0, pageSize) : rows;
        List<PostResponse> items = page.stream().map(PostResponse::from).toList();
        Long nextCursor = hasMore ? page.get(page.size() - 1).getId() : null;
        return CursorPageResponse.of(items, nextCursor, hasMore);
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
