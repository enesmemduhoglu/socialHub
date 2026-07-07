package com.enes.social.comment.service;

import com.enes.social.comment.dto.CommentResponse;
import com.enes.social.comment.dto.CreateCommentRequest;
import com.enes.social.comment.model.Comment;
import com.enes.social.comment.repository.CommentRepository;
import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.common.exception.ForbiddenException;
import com.enes.social.common.exception.ResourceNotFoundException;
import com.enes.social.post.repository.PostRepository;
import com.enes.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Yorum iş mantığı: oluşturma, keyset listeleme ve sahiplik kontrollü silme.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 50;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse create(Long authorId, Long postId, CreateCommentRequest request) {
        requirePostExists(postId);
        Comment comment = Comment.builder()
                .post(postRepository.getReferenceById(postId))
                .author(userRepository.getReferenceById(authorId))
                .content(request.content().trim())
                .build();
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponse> list(Long postId, Long cursor, Integer size) {
        requirePostExists(postId);
        int pageSize = normalizeSize(size);
        List<Comment> rows = commentRepository.findByPost(postId, cursor, Limit.of(pageSize + 1));

        boolean hasMore = rows.size() > pageSize;
        List<Comment> page = hasMore ? rows.subList(0, pageSize) : rows;
        List<CommentResponse> items = page.stream().map(CommentResponse::from).toList();
        Long nextCursor = hasMore ? page.get(page.size() - 1).getId() : null;
        return CursorPageResponse.of(items, nextCursor, hasMore);
    }

    @Transactional
    public void delete(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum bulunamadı: " + commentId));
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("Bu yorum üzerinde işlem yapma yetkiniz yok");
        }
        commentRepository.delete(comment);
    }

    private void requirePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Gönderi bulunamadı: " + postId);
        }
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
