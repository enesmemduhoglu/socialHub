package com.enes.social.post.service;

import com.enes.social.comment.repository.CommentRepository;
import com.enes.social.like.repository.PostLikeRepository;
import com.enes.social.post.dto.PostIdCount;
import com.enes.social.post.dto.PostResponse;
import com.enes.social.post.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gönderi listeleri için beğeni/yorum sayılarını ve mevcut kullanıcının
 * beğeni durumunu 3 toplu sorguyla yükler — gönderi başına sorgu (N+1) yok.
 */
@Component
@RequiredArgsConstructor
public class PostResponseAssembler {

    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    public Function<Post, PostResponse> mapper(List<Post> posts, Long currentUserId) {
        if (posts.isEmpty()) {
            return PostResponse::fresh;
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Long> likeCounts = toCountMap(postLikeRepository.countByPostIds(postIds));
        Map<Long, Long> commentCounts = toCountMap(commentRepository.countByPostIds(postIds));
        Set<Long> likedIds = Set.copyOf(postLikeRepository.findLikedPostIds(currentUserId, postIds));

        return post -> PostResponse.from(
                post,
                likeCounts.getOrDefault(post.getId(), 0L),
                commentCounts.getOrDefault(post.getId(), 0L),
                likedIds.contains(post.getId())
        );
    }

    private static Map<Long, Long> toCountMap(List<PostIdCount> rows) {
        return rows.stream()
                .collect(Collectors.toMap(PostIdCount::getPostId, PostIdCount::getCnt));
    }
}
