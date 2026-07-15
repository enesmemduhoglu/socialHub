package com.enes.social.feed.service;

import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.follow.service.FollowService;
import com.enes.social.post.dto.PostResponse;
import com.enes.social.post.model.Post;
import com.enes.social.post.repository.PostRepository;
import com.enes.social.post.service.PostResponseAssembler;
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

    private final FollowService followService;
    private final PostRepository postRepository;
    private final PostResponseAssembler assembler;

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> timeline(Long userId, Long cursor, Integer size) {
        List<Long> authorIds = followService.followeeIdsIncludingSelf(userId);
        int pageSize = CursorPageResponse.normalizeSize(size);

        List<Post> rows = postRepository.findTimeline(authorIds, cursor, Limit.of(pageSize + 1));
        return CursorPageResponse.paginate(rows, pageSize,
                assembler.mapper(rows, userId), Post::getId);
    }
}
